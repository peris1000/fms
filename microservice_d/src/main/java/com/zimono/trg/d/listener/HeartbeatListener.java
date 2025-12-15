package com.zimono.trg.d.listener;

import com.zimono.trg.d.CarWebSocket;
import com.zimono.trg.shared.Heartbeat;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consumer of heartbeats produced by microservice_b.
 */
@ApplicationScoped
public class HeartbeatListener {

    private static final Logger LOG = LoggerFactory.getLogger(HeartbeatListener.class);

    @Inject
    CarWebSocket carWebSocket;

    @Incoming("car-heartbeats")
    public void consume(Heartbeat heartbeat) {
        LOG.debug("Trip: {} has new heartbeat", heartbeat.tripId());
        // Forward to all websocket clients
        carWebSocket.broadcast(heartbeat);
    }

}
