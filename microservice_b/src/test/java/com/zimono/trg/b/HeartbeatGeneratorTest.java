package com.zimono.trg.b;

import com.zimono.trg.b.service.ConfigParams;
import com.zimono.trg.b.service.HeartbeatGenerator;
import com.zimono.trg.shared.Heartbeat;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class HeartbeatGeneratorTest {

    @Inject
    HeartbeatGenerator generator;

    @Test
    void generatedHeartbeat_within_configured_bounds() {
        ConfigParams params = new ConfigParams(30.0, 100.0, 40.7128, -74.0060, 0.1);
        Heartbeat hb = generator.generateHeartbeat(params, 1L, 2L, 3L);
        assertNotNull(hb);
        assertNotNull(hb.timestamp());
        assertEquals(1L, hb.tripId());
        assertEquals(2L, hb.carId());
        assertEquals(3L, hb.driverId());

        // Defaults in HeartbeatGenerator (min=30, max=100, dev=0.1, base lat/lon)
        assertTrue(hb.speed() >= 0.0); // basic sanity
        assertTrue(hb.latitude() <= 40.7128 + 0.1 && hb.latitude() >= 40.7128 - 0.1);
        assertTrue(hb.longitude() <= -74.0060 + 0.1 && hb.longitude() >= -74.0060 - 0.1);
    }
}
