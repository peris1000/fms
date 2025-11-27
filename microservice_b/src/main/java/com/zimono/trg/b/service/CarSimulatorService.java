package com.zimono.trg.b.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimono.trg.shared.TripMessage;
import com.zimono.trg.shared.Heartbeat;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.MutinyEmitter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@ApplicationScoped
public class CarSimulatorService {

    private static final Logger LOG = LoggerFactory.getLogger(CarSimulatorService.class);

    @ConfigProperty(name = "simulation.speed.min", defaultValue = "30.0")
    double minSpeed;
    @ConfigProperty(name = "simulation.speed.max", defaultValue = "100.0")
    double maxSpeed;
    @ConfigProperty(name = "simulation.location.base-latitude", defaultValue = "40.7128")
    double baseLatitude; // -90deg - 90deg
    @ConfigProperty(name = "simulation.location.base-longitude", defaultValue = "-74.0060")
    double baseLongitude; // -180deg - 180deg
    @ConfigProperty(name = "simulation.location.deviation", defaultValue = "0.1")
    double deviation;

    @Channel("car-heartbeats")
//    @OnOverflow(OnOverflow.Strategy.DROP)
//    @OnOverflow(value = OnOverflow.Strategy.BUFFER, bufferSize = 30000)
    Emitter<Heartbeat> heartbeatEmitter;
//    MutinyEmitter<Heartbeat> heartbeatEmitter;

    @Inject
    HeartbeatGenerator heartbeatGenerator;

    private volatile boolean running = false;
    private AtomicInteger counter = new AtomicInteger(0);
    // storage for trip assignments
    private static final ConcurrentHashMap<Long, TripMessage> TRIPS = new ConcurrentHashMap<>();
    // helper to avoid race conditions
    private static final Set<Long> REMOVALS = ConcurrentHashMap.newKeySet();

    void onStart(@Observes StartupEvent ev) {
        // just to know that the service has started
        LOG.info("Car Simulator Service starting...");
        running = true;
    }
    void onStop(@Observes ShutdownEvent ev) {
        running = false;
    }

    @Scheduled(every = "{simulation.interval:10s}")
    public void generateScheduledHeartbeat() {

        if (!running) {
            return; // avoid emitting after shutdown
        }
        if (TRIPS.isEmpty()) {
            LOG.debug("No active trips to simulate");
            return;
        }

        ConfigParams params = new ConfigParams(minSpeed, maxSpeed, baseLatitude, baseLongitude, deviation);
        // pseudo validate
        if (params.minSpeed() < 0 || params.maxSpeed() < 0 || params.minSpeed() >= params.maxSpeed() || params.deviation() <= 0
                || params.baseLatitude() >= 90 || params.baseLatitude() <= -90
                || params.baseLongitude() >= 180 || params.baseLongitude() <= -180) {
            LOG.warn("Strange simulation configuration values found...");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Generating {} heartbeats", TRIPS.size());
        }

        // for sending high traffic (with buffering setup)
//        List<CompletableFuture<Void>> futures = TRIPS.entrySet().stream()
//                .filter(entry -> !REMOVALS.contains(entry.getKey()) && running)
//                .map(entry -> CompletableFuture.runAsync(() -> {
//                    try {
//                        TripMessage trip = entry.getValue();
//                        Heartbeat heartbeat = heartbeatGenerator.generateHeartbeat(
//                                params, entry.getKey(), trip.carId(), trip.driverId()
//                        );
//                        heartbeatEmitter.send(heartbeat);
//                        LOG.debug("Sent heartbeat: Trip={}, Speed={} Km/h", entry.getKey(), heartbeat.speed());
//                    } catch (Exception e) {
//                        LOG.error("Failed to generate heartbeat for trip {}", entry.getKey(), e);
//                    }
//                }))
//                .collect(Collectors.toList());
//
//        // Wait for all to complete (with timeout)
//        try {
//            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(5, TimeUnit.SECONDS);
//            LOG.info("Successfully sent {} heartbeats", futures.size());
//        } catch (TimeoutException e) {
//            LOG.warn("Heartbeat generation timed out after 5 seconds");
//        } catch (Exception e) {
//            LOG.error("Error during heartbeat generation", e);
//        }


        // for sending high traffic (without buffering)
        Multi.createFrom().items(TRIPS.entrySet().stream()
                        .filter(entry -> !REMOVALS.contains(entry.getKey()))
                        .map(Map.Entry::getValue))
                .onItem().transform(trip ->
                        heartbeatGenerator.generateHeartbeat(params,
                                trip.tripId(), trip.carId(), trip.driverId())
                )
                .onItem().transformToUniAndMerge(this::sendReactive)
                .subscribe().with(
                        v -> {},
                        err -> LOG.error("Stream error", err)
                );
    }

    private Uni<Void> sendReactive(Heartbeat hb) {
        return Uni.createFrom()
                .completionStage(() -> heartbeatEmitter.send(hb));
    }

    public boolean addTrip(TripMessage message) {
        if (message == null || message.tripId() <= 0) {
            LOG.warn("Cannot add null trip or trip with faulty trip id.");
            return false;
        }
        // Don't add if removal is in progress
        if (REMOVALS.contains(message.tripId())) {
            LOG.warn("Trip {} is being removed, cannot add.", message.tripId());
            return false;
        }

        TripMessage existing = TRIPS.putIfAbsent(message.tripId(), message);
        if (existing == null) {
            LOG.info("Added trip {} to simulator.", message.tripId());
            return true;
        }
        return false;
    }

    public void removeTrip(Long tripId) {
        if (tripId == null) {
            LOG.warn("Cannot remove trip with null id.");
            return;
        }

        // Mark as being removed
        REMOVALS.add(tripId);

        try {
            TripMessage removed = TRIPS.remove(tripId);
            if (removed == null) {
                LOG.warn("Trip {} not found in processing map", tripId);
            } else {
                LOG.info("Removed trip {} from simulator", tripId);
            }
        } finally {
            // Cleanup after a delay to prevent race conditions
            CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS)
                    .execute(() -> REMOVALS.remove(tripId));
        }
    }
}
