package com.zimono.trg.a.service;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class CacheInvalidationServiceTest {

    @Inject
    CacheInvalidationService cacheInvalidationService;

    @Test
    public void testCacheAvailability() {
        assertTrue(cacheInvalidationService.isDriverCacheAvailable());
        assertTrue(cacheInvalidationService.isCarCacheAvailable());
        assertTrue(cacheInvalidationService.isTripCacheAvailable());
    }

    @Test
    public void testInvalidateDriverCache() {
        // This should not throw an exception
        assertDoesNotThrow(() -> cacheInvalidationService.invalidateDriverCache(1L));
    }

    @Test
    public void testInvalidateCarCache() {
        // This should not throw an exception
        assertDoesNotThrow(() -> cacheInvalidationService.invalidateCarCache(1L));
    }

    @Test
    public void testInvalidateTripCache() {
        // This should not throw an exception
        assertDoesNotThrow(() -> cacheInvalidationService.invalidateTripCache(1L));
    }

    @Test
    public void testInvalidateNullId() {
        // Should handle null gracefully
        assertDoesNotThrow(() -> cacheInvalidationService.invalidateDriverCache(null));
        assertDoesNotThrow(() -> cacheInvalidationService.invalidateCarCache(null));
        assertDoesNotThrow(() -> cacheInvalidationService.invalidateTripCache(null));
    }
}