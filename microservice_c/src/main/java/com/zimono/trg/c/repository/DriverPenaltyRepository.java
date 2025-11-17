package com.zimono.trg.c.repository;

import com.zimono.trg.c.model.DriverPenalty;
import com.zimono.trg.shared.net.TripPenaltiesDto;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class DriverPenaltyRepository implements PanacheRepository<DriverPenalty> {
    private static final Logger LOG = LoggerFactory.getLogger(DriverPenaltyRepository.class);

    @Inject
    EntityManager entityManager;

    public Uni<List<DriverPenalty>> findByDriverId(Long driverId) {
        if (driverId == null || driverId <= 0) {
            LOG.warn("Invalid driverId: {}", driverId);
            return Uni.createFrom().item(Collections.emptyList());
        }

        return Uni.createFrom().item(() -> {
            List<DriverPenalty> results = find("driverId = ?1", driverId).list();
            return results;
        }).onFailure().invoke(throwable ->
                LOG.error("Failed to find penalties for driver {}", driverId, throwable)
        );
    }

    public Uni<TripPenaltiesDto> findByTripId(Long tripId) {
        if (tripId == null || tripId <= 0) {
            LOG.warn("Invalid tripId: {}", tripId);
            return Uni.createFrom().nullItem();
        }

        return Uni.createFrom().item(() -> {
            List<DriverPenalty> penalties = find("tripId = ?1", tripId).list();
            int totalPoints = penalties.stream().mapToInt(DriverPenalty::getPenaltyPoints).sum();
            long carId = penalties.isEmpty() ? 0L : penalties.get(0).getCarId();
            long driverId = penalties.isEmpty() ? 0L : penalties.get(0).getDriverId();
            return new TripPenaltiesDto(tripId, carId, driverId, totalPoints, Instant.now());
        }).onFailure().invoke(throwable ->
                LOG.error("Failed to find penalties for trip {}", tripId, throwable)
        );
    }

    @Transactional
    public Uni<DriverPenalty> persistAsync(DriverPenalty penalty) {
        if (penalty == null) {
            LOG.error("Cannot persist null penalty");
            return Uni.createFrom().failure(new IllegalArgumentException("Penalty cannot be null"));
        }
        return Uni.createFrom().item(() -> {
            entityManager.persist(penalty);
            return penalty;
        }).onFailure().invoke(throwable ->
                LOG.error("Failed to persist penalty for driver {}", penalty.getDriverId(), throwable)
        );
    }

    public Uni<List<DriverPenalty>> getPenaltiesByDriverId(Long driverId) {
        return findByDriverId(driverId);
    }
}
