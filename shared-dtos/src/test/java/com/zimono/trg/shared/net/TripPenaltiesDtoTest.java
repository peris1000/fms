package com.zimono.trg.shared.net;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TripPenaltiesDtoTest {

    @Test
    void recordAccessorsReturnAssignedValues() {
        Instant now = Instant.now();
        TripPenaltiesDto dto = new TripPenaltiesDto(123L, 456L, 789L, 15, now);

        assertEquals(123L, dto.trip_id());
        assertEquals(456L, dto.car_id());
        assertEquals(789L, dto.driver_d());
        assertEquals(15, dto.total_penalty_points());
        assertEquals(now, dto.created());

        // Equality and toString basic sanity
        TripPenaltiesDto dto2 = new TripPenaltiesDto(123L, 456L, 789L, 15, now);
        assertEquals(dto, dto2);
        assertEquals(dto.hashCode(), dto2.hashCode());
        assertTrue(dto.toString().contains("trip_id=123"));
    }
}
