package com.zimono.trg.a.listener;

import com.zimono.trg.a.model.Driver;
import com.zimono.trg.a.repository.DriverRepository;
import com.zimono.trg.a.service.CacheInvalidationService;
import com.zimono.trg.shared.DriverPenaltyMessage;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

@ApplicationScoped
public class DriverPenaltyListener {
    private static final Logger LOG = LoggerFactory.getLogger(DriverPenaltyListener.class);

    @Inject
    DriverRepository repo;


    @Inject
    CacheInvalidationService cacheInvalidationService;

    @Incoming("driver-penalties")
    @Transactional
    public Uni<Void> processDriverPenalties(DriverPenaltyMessage message) {
        return Uni.createFrom().item(() -> {
            LOG.info("Processing driver penalty: Driver={}, PenaltyPoints={}", message.driverId(), message.penaltyPoints());

            if (message.driverId() <= 0 || message.penaltyPoints() <= 0) {
                LOG.warn("Invalid penalty message: {}", message);
                return null;
            }

            Driver driver = repo.findById(message.driverId());
            if (driver == null) {
                LOG.error("Cannot apply penalty: Driver {} not found", message.driverId());
                return null;
            }

            // Invalidate cache
            cacheInvalidationService.invalidateDriverCache(message.driverId());

            // Update penalty points on the fly
            int result = repo.updateDriverPenalties(message.driverId(), message.penaltyPoints(), Instant.now());
            if (result == 0) {
                LOG.error("Cannot apply penalty: Driver {} not found", message.driverId());
            } else {
                LOG.info("Applied {} penalty points to driver {}", message.penaltyPoints(), message.driverId());
            }
            return null;
        }).replaceWithVoid();
    }
}
