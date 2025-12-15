package com.zimono.trg.d.listener;

import com.zimono.trg.d.CarWebSocket;
import com.zimono.trg.shared.Heartbeat;
import com.zimono.trg.shared.TripMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Consumer of trip stops produced by microservice_a.
 */
@ApplicationScoped
public class TripListener {
    private static final Logger LOG = LoggerFactory.getLogger(TripListener.class);

    @Inject
    CarWebSocket carWebSocket;

    @Incoming("trip-stops")
    public CompletionStage<Void> consume(Message<TripMessage> message) {
        return CompletableFuture.runAsync(() -> {
            try {
                TripMessage trip = message.getPayload();
                LOG.debug("Trip: {} is getting stopped", trip.tripId());

                // Static dto to alert the websocket clients
                Heartbeat beat = new Heartbeat(trip.tripId(), -1L, trip.driverId(), 0d, 0d, 0d, Instant.now());
                // Forward to all websocket clients
                carWebSocket.broadcast(beat);
            } catch (Exception e) {
                LOG.error("Error processing trip stop", e);
                throw new RuntimeException("Failed to process trip stop", e);
            }
        }).thenRun(() -> message.ack());
    }
}
