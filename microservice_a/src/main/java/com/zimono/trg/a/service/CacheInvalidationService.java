package com.zimono.trg.a.service;

import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheManager;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@ApplicationScoped
public class CacheInvalidationService {
    private static final Logger LOG = LoggerFactory.getLogger(CacheInvalidationService.class.getName());

    @Inject
    CacheManager cacheManager;

    public void invalidateDriverCache(Long driverId) {
        try {
            Optional<Cache> cacheOpt = cacheManager.getCache("drivers-cache");
            if (cacheOpt.isPresent() && driverId != null) {
                Cache cache = cacheOpt.get();
                // Invalidate returns Uni<Void> - we need to await the result
                cache.invalidate(driverId).await().indefinitely(); // Block until completion
                LOG.debug("Invalidated driver cache for driverId: {}", driverId);
            } else {
                LOG.warn("Drivers cache not found or driverId is null: {}", driverId);
            }
        } catch (Exception e) {
            LOG.warn("Failed to invalidate driver cache for driverId: {}. Error: {}", driverId, e.getMessage());
        }
    }

    public void invalidateAllDriverCaches() {
        try {
            Optional<Cache> cacheOpt = cacheManager.getCache("drivers-cache");
            if (cacheOpt.isPresent()) {
                Cache cache = cacheOpt.get();
                cache.invalidateAll().await().indefinitely(); // Block until completion
                LOG.debug("Invalidated all driver caches");
            } else {
                LOG.warn("Drivers cache not found when trying to invalidate all");
            }
        } catch (Exception e) {
            LOG.warn("Failed to invalidate all driver caches. Error: {}", e.getMessage());
        }
    }

    // Reactive methods that return Uni<Void> for non-blocking operations
    public Uni<Void> invalidateDriverCacheAsync(Long driverId) {
        return Uni.createFrom().deferred(() -> {
            Optional<Cache> cacheOpt = cacheManager.getCache("drivers-cache");
            if (cacheOpt.isPresent() && driverId != null) {
                Cache cache = cacheOpt.get();
                return cache.invalidate(driverId)
                        .onItem().invoke(() -> LOG.debug("Invalidated driver cache for driverId: {}", driverId))
                        .onFailure().invoke(failure -> LOG.warn("Failed to invalidate driver cache: {}", failure.getMessage()));
            } else {
                LOG.warn("Drivers cache not found or driverId is null: {}", driverId);
                return Uni.createFrom().voidItem();
            }
        });
    }

    public void invalidateCarCache(Long carId) {
        try {
            Optional<Cache> cacheOpt = cacheManager.getCache("cars-cache");
            if (cacheOpt.isPresent() && carId != null) {
                Cache cache = cacheOpt.get();
                cache.invalidate(carId).await().indefinitely(); // Block until completion
                LOG.info("Invalidated car cache for carId: {}", carId);
            } else {
                LOG.warn("Cars cache not found or carId is null: {}", carId);
            }
        } catch (Exception e) {
            LOG.warn("Failed to invalidate car cache for carId: {}. Error: {}", carId, e.getMessage());
        }
    }

    public void invalidateAllCarCaches() {
        try {
            Optional<Cache> cacheOpt = cacheManager.getCache("cars-cache");
            if (cacheOpt.isPresent()) {
                Cache cache = cacheOpt.get();
                cache.invalidateAll().await().indefinitely();
                LOG.debug("Invalidated all car caches");
            } else {
                LOG.warn("Cars cache not found when trying to invalidate all");
            }
        } catch (Exception e) {
            LOG.warn("Failed to invalidate all car caches. Error: {}", e.getMessage());
        }
    }


    // Reactive methods that return Uni<Void> for non-blocking operations
    public Uni<Void> invalidateCarCacheAsync(Long carId) {
        return Uni.createFrom().deferred(() -> {
            Optional<Cache> cacheOpt = cacheManager.getCache("cars-cache");
            if (cacheOpt.isPresent() && carId != null) {
                Cache cache = cacheOpt.get();
                return cache.invalidate(carId)
                        .onItem().invoke(() -> LOG.debug("Invalidated car cache for carId: {}", carId))
                        .onFailure().invoke(failure -> LOG.warn("Failed to invalidate car cache: {}", failure.getMessage()));
            } else {
                LOG.warn("Cars cache not found or carId is null: {}", carId);
                return Uni.createFrom().voidItem();
            }
        });
    }

    public void invalidateTripCache(Long tripId) {
        try {
            Optional<Cache> cacheOpt = cacheManager.getCache("trips-cache");
            if (cacheOpt.isPresent() && tripId != null) {
                Cache tripsCache = cacheOpt.get();
                // Invalidate returns Uni<Void> - we need to await the result
                tripsCache.invalidate(tripId).await().indefinitely(); // Block until completion
                LOG.debug("Invalidated trip cache for tripId: {}", tripId);
            } else {
                LOG.warn("Trips cache not found or tripId is null: {}", tripId);
            }
        } catch (Exception e) {
            LOG.warn("Failed to invalidate trip cache for tripId: {}. Error: {}", tripId, e.getMessage());
        }
    }

    public void invalidateAllTripCaches() {
        try {
            Optional<Cache> cacheOpt = cacheManager.getCache("trips-cache");
            if (cacheOpt.isPresent()) {
                Cache cache = cacheOpt.get();
                cache.invalidateAll().await().indefinitely(); // Block until completion
                LOG.debug("Invalidated all trip caches");
            } else {
                LOG.warn("Trips cache not found when trying to invalidate all");
            }
        } catch (Exception e) {
            LOG.warn("Failed to invalidate all trip caches. Error: {}", e.getMessage());
        }
    }

    // Reactive methods that return Uni<Void> for non-blocking operations
    public Uni<Void> invalidateTripCacheAsync(Long tripId) {
        return Uni.createFrom().deferred(() -> {
            Optional<Cache> cacheOpt = cacheManager.getCache("trips-cache");
            if (cacheOpt.isPresent() && tripId != null) {
                Cache cache = cacheOpt.get();
                return cache.invalidate(tripId)
                        .onItem().invoke(() -> LOG.debug("Invalidated trip cache for tripId: {}", tripId))
                        .onFailure().invoke(failure -> LOG.warn("Failed to invalidate trip cache: {}", failure.getMessage()));
            } else {
                LOG.warn("Trips cache not found or tripId is null: {}", tripId);
                return Uni.createFrom().voidItem();
            }
        });
    }

    public boolean isDriverCacheAvailable() {
        return cacheManager.getCache("drivers-cache").isPresent();
    }
    public boolean isCarCacheAvailable() {
        return cacheManager.getCache("cars-cache").isPresent();
    }
    public boolean isTripCacheAvailable() {
        return cacheManager.getCache("trips-cache").isPresent();
    }

    // Method to get cache names for debugging
    public void logAvailableCaches() {
        LOG.info("Available caches: {}", cacheManager.getCacheNames());
    }
}
