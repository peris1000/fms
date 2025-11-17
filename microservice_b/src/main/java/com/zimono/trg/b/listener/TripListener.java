package com.zimono.trg.b.listener;

import com.zimono.trg.b.service.CarSimulatorService;
import com.zimono.trg.shared.TripMessage;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class TripListener {
    private static final Logger LOG = LoggerFactory.getLogger(TripListener.class);

    @Inject
    CarSimulatorService carSimulatorService;

    @Incoming("trip-starts")
    @Transactional
    public CompletionStage<Void> processStarts(Message<TripMessage> message) {
        return CompletableFuture.runAsync(() -> {
            try {
                TripMessage trip = message.getPayload();
                LOG.debug("Trip {} getting added to simulator.", trip.tripId());

                boolean added = carSimulatorService.addTrip(trip);
                if (!added) {
                    LOG.warn("Failed to add trip {} to simulator", trip.tripId());
                }
            } catch (Exception e) {
                LOG.error("Error adding trip to simulator.", e);
                throw new RuntimeException("Failed to add trip to simulator.", e);
            }
        }).thenRun(() -> message.ack());
    }

    @Incoming("trip-stops")
    @Transactional
    public CompletionStage<Void> processStops(Message<TripMessage> message) {
        return CompletableFuture.runAsync(() -> {
            try {
                TripMessage trip = message.getPayload();
                LOG.debug("Stopping the trip: {}", trip.tripId());

                carSimulatorService.removeTrip(trip.tripId());
            } catch (Exception e) {
                LOG.error("Error processing trip stop", e);
                throw new RuntimeException("Failed to process trip stop", e);
            }
        }).thenRun(() -> message.ack());
    }
}
