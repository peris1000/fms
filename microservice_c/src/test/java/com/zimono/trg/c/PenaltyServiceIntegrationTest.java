package com.zimono.trg.c;

import com.zimono.trg.c.model.DriverPenalty;
import com.zimono.trg.c.repository.DriverPenaltyRepository;
import com.zimono.trg.c.listener.HeartbeatListener;
import com.zimono.trg.shared.Heartbeat;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PenaltyServiceIntegrationTest {

    @Inject
    HeartbeatListener penaltyService;
    @Inject
    DriverPenaltyRepository driverPenaltyRepository;

    @Test
    @Order(1)
    @Transactional
    public void testProcessHeartbeatWithPenalty() {
        // Test with speed that should generate penalty
        Heartbeat heartbeat = new Heartbeat(
                1L, 1L, 1L, 40.7128, -74.0060, 70.0, Instant.now());

        penaltyService.processHeartbeat(heartbeat)
                .await().indefinitely();

        // Verify penalty was added
        DriverPenalty penalty = driverPenaltyRepository.findById(1L);
        assertNotNull(penalty);
        assertEquals(20, penalty.getPenaltyPoints()); // 20 points for 70 Km/h
    }

    @Test
    @Order(2)
    @Transactional
    public void testProcessHeartbeatNoPenalty() {
        // Test with speed that should NOT generate penalty
        Heartbeat heartbeat = new Heartbeat(
                2L, 2L, 2L, 40.7128, -74.0060, 50.0, Instant.now());

        penaltyService.processHeartbeat(heartbeat)
                .await().indefinitely();

        // Verify no penalty was added
        DriverPenalty penalty = driverPenaltyRepository.findById(2L);
        assertNull(penalty); // Should not create entry for no penalty
    }
}
