package com.zimono.trg.c.repository;

import com.zimono.trg.c.model.DriverPenalty;
import com.zimono.trg.shared.net.TripPenaltiesDto;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DriverPenaltyRepositoryTest {

    @Inject
    DriverPenaltyRepository repository;

    @Test
    @Order(1)
    void findByDriverId_invalid_returnsEmptyList() {
        // null
        List<DriverPenalty> nullResult = repository.findByDriverId(null)
                .await().indefinitely();
        assertNotNull(nullResult);
        assertTrue(nullResult.isEmpty());

        // <= 0
        List<DriverPenalty> zeroResult = repository.findByDriverId(0L)
                .await().indefinitely();
        assertNotNull(zeroResult);
        assertTrue(zeroResult.isEmpty());

        List<DriverPenalty> negativeResult = repository.findByDriverId(-5L)
                .await().indefinitely();
        assertNotNull(negativeResult);
        assertTrue(negativeResult.isEmpty());
    }

    @Test
    @Order(2)
    @Transactional
    void findByDriverId_withData_returnsList() {
        // given persisted penalties for driver 11
        DriverPenalty p1 = new DriverPenalty(101L, 201L, 11L, 10);
        DriverPenalty p2 = new DriverPenalty(102L, 202L, 11L, 5);
        repository.persistAsync(p1).await().indefinitely();
        repository.persistAsync(p2).await().indefinitely();

        // when
        List<DriverPenalty> list = repository.findByDriverId(11L).await().indefinitely();

        // then
        assertNotNull(list);
        assertEquals(2, list.size());
        assertTrue(list.stream().anyMatch(dp -> dp.getTripId() == 101L && dp.getPenaltyPoints() == 10));
        assertTrue(list.stream().anyMatch(dp -> dp.getTripId() == 102L && dp.getPenaltyPoints() == 5));
    }

    @Test
    @Order(3)
    void findByTripId_invalid_returnsNullItem() {
        TripPenaltiesDto nullTrip = repository.findByTripId(null).await().indefinitely();
        assertNull(nullTrip);

        TripPenaltiesDto zeroTrip = repository.findByTripId(0L).await().indefinitely();
        assertNull(zeroTrip);

        TripPenaltiesDto negativeTrip = repository.findByTripId(-7L).await().indefinitely();
        assertNull(negativeTrip);
    }

    @Test
    @Order(4)
    void findByTripId_noData_returnsZeros() {
        Long tripId = 99999L;
        TripPenaltiesDto dto = repository.findByTripId(tripId).await().indefinitely();
        assertNotNull(dto);
        assertEquals(tripId.longValue(), dto.trip_id());
        assertEquals(0L, dto.car_id());
        assertEquals(0L, dto.driver_d());
        assertEquals(0, dto.total_penalty_points());
        assertNotNull(dto.created());
        assertTrue(dto.created().isBefore(Instant.now().plusSeconds(5)));
    }

    @Test
    @Order(5)
    @Transactional
    void findByTripId_withData_sumsAndCopiesIds() {
        long tripId = 1234L;
        // Persist two penalties for same trip and different points
        DriverPenalty p1 = new DriverPenalty(tripId, 3001L, 4001L, 7);
        DriverPenalty p2 = new DriverPenalty(tripId, 3001L, 4001L, 13);
        repository.persistAsync(p1).await().indefinitely();
        repository.persistAsync(p2).await().indefinitely();

        TripPenaltiesDto dto = repository.findByTripId(tripId).await().indefinitely();
        assertNotNull(dto);
        assertEquals(tripId, dto.trip_id());
        // car/driver ids come from first element when list not empty
        assertEquals(3001L, dto.car_id());
        assertEquals(4001L, dto.driver_d());
        assertEquals(20, dto.total_penalty_points());
        assertNotNull(dto.created());
    }

    @Test
    @Order(6)
    void persistAsync_null_throws() {
        Uni<DriverPenalty> uni = repository.persistAsync(null);
        assertThrows(IllegalArgumentException.class, () -> uni.await().indefinitely());
    }

    @Test
    @Order(7)
    @Transactional
    void persistAsync_valid_persistsEntity() {
        DriverPenalty p = new DriverPenalty(888L, 777L, 666L, 42);
        DriverPenalty saved = repository.persistAsync(p).await().indefinitely();
        assertNotNull(saved);
        assertTrue(saved.getId() > 0);
        assertEquals(888L, saved.getTripId());
        assertEquals(777L, saved.getCarId());
        assertEquals(666L, saved.getDriverId());
        assertEquals(42, saved.getPenaltyPoints());

        // Verify reachable via repository query
        List<DriverPenalty> list = repository.findByDriverId(666L).await().indefinitely();
        assertFalse(list.isEmpty());
        assertTrue(list.stream().anyMatch(dp -> dp.getTripId() == 888L && dp.getPenaltyPoints() == 42));
    }
}
