package com.zimono.trg.c.listener;

import com.zimono.trg.c.utils.PenaltyCalculator;
import com.zimono.trg.shared.DriverPenaltyMessage;
import com.zimono.trg.shared.Heartbeat;
import com.zimono.trg.c.model.DriverPenalty;
import com.zimono.trg.c.repository.DriverPenaltyRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * The recipient of heartbeats produced by microservice_b.
 */
@ApplicationScoped
public class HeartbeatListener {

    private static final Logger LOG = LoggerFactory.getLogger(HeartbeatListener.class);

    @Inject
    DriverPenaltyRepository repo;
    @Channel("driver-penalties")
    Emitter<DriverPenaltyMessage> penaltiesEmitter;

    @Incoming("car-heartbeats")
    @Transactional
    public Uni<Void> processHeartbeat(Heartbeat heartbeat) {
        LOG.info("Processing heartbeat: Driver={}, Speed={}", heartbeat.driverId(), heartbeat.speed());

        int penaltyPoints = PenaltyCalculator.calculatePenaltyPoints(heartbeat.speed());
        if (penaltyPoints > 0) {

            // While penalty points are addresses to a driver we need to store it as evidence
            // and notify the microservice_a as owner of the driver.

            // create a new obj, add penalty points to it, store it and notify microservice_a
            return Uni.createFrom().item(createNewPenalty(heartbeat))
                    .onItem().transform(penalty -> {
                        penalty.addPenaltyPoints(penaltyPoints);
                        return penalty;
                    })
                    .onItem().transformToUni(penalty -> repo.persistAsync(penalty))
                    .onItem().invoke(penalty -> {
                        LOG.info("Added {} penalty points to driver {}", penaltyPoints, heartbeat.driverId());

                        // send to driver-penalties channel (microservice_a)
                        penaltiesEmitter.send(new DriverPenaltyMessage(heartbeat.driverId(), penaltyPoints));
                    })
                    .replaceWithVoid();
        }
        return Uni.createFrom().voidItem();
    }

    private DriverPenalty createNewPenalty(Heartbeat heartbeat) {
        DriverPenalty penalty = new DriverPenalty();
        penalty.setTripId(heartbeat.tripId());
        penalty.setCarId(heartbeat.carId());
        penalty.setDriverId(heartbeat.driverId());
        penalty.setPenaltyPoints(0);
        penalty.setCreatedAt(Instant.now());
        return penalty;
    }
}
