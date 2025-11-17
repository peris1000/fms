package com.zimono.trg.c.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class DriverPenaltyTest {

    @Test
    void gettersSettersAndAddPointsWork() {
        DriverPenalty dp = new DriverPenalty();
        dp.setId(10L);
        dp.setTripId(1001L);
        dp.setCarId(2002L);
        dp.setDriverId(3003L);
        dp.setPenaltyPoints(5);
        Instant now = Instant.now();
        dp.setCreatedAt(now);

        assertEquals(10L, dp.getId());
        assertEquals(1001L, dp.getTripId());
        assertEquals(2002L, dp.getCarId());
        assertEquals(3003L, dp.getDriverId());
        assertEquals(5, dp.getPenaltyPoints());
        assertEquals(now, dp.getCreatedAt());

        dp.addPenaltyPoints(7);
        assertEquals(12, dp.getPenaltyPoints());
    }

    @Test
    void constructorAndCombineSumPoints() {
        DriverPenalty a = new DriverPenalty(1L, 2L, 3L, 4);
        DriverPenalty b = new DriverPenalty(1L, 2L, 3L, 6);

        DriverPenalty combined = a.combine(b);

        assertEquals(1L, combined.getTripId());
        assertEquals(2L, combined.getCarId());
        assertEquals(3L, combined.getDriverId());
        assertEquals(10, combined.getPenaltyPoints());
        assertNotNull(combined.getCreatedAt());
    }
}
