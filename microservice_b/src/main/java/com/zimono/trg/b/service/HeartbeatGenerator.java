package com.zimono.trg.b.service;

import com.zimono.trg.shared.Heartbeat;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.util.Random;

@ApplicationScoped
public class HeartbeatGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(HeartbeatGenerator.class);


    private final Random random = new Random();

    public Heartbeat generateHeartbeat(ConfigParams params, Long tripId, Long carId, Long driverId) {
        // Simulate movement and speed changes
        double lat = params.baseLatitude() + (random.nextDouble() - 0.5) * params.deviation(); // default moving ~ 0.05deg from base
        double lon = params.baseLongitude() + (random.nextDouble() - 0.5) * params.deviation();
        double speed = params.minSpeed() + random.nextDouble() * (params.maxSpeed() - params.minSpeed()); // default 30-100 Km/h

        return new Heartbeat(tripId, carId, driverId, lat, lon, speed, Instant.now());
    }
}
