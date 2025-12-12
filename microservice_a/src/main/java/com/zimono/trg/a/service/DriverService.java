package com.zimono.trg.a.service;


import com.zimono.trg.a.dto.DriverDto;
import com.zimono.trg.a.model.Driver;
import com.zimono.trg.a.repository.CarRepository;
import com.zimono.trg.a.repository.DriverRepository;
import com.zimono.trg.a.repository.TripRepository;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class DriverService {
    private static final Logger LOG = LoggerFactory.getLogger(DriverService.class.getName());

    @Inject
    CacheInvalidationService cacheInvalidationService;
    @Inject
    DriverRepository repo;
    @Inject
    CarRepository carRepo;
    @Inject
    TripRepository tripRepo;

    public List<Driver> getAllDrivers() {
        return repo.listAll();
    }

    @CacheResult(cacheName = "drivers-cache")
    public Driver getDriverById(Long id) {
        return repo.findByIdOptional(id).orElseThrow(
            () -> new NotFoundException("Driver not found with id: " + id)
        );
    }

    @Transactional
    public Driver createDriver(DriverDto dto) {
        Driver entity = new Driver();
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmail(dto.getEmail());
        entity.setDrivingLicense(dto.getDrivingLicense());
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        repo.persist(entity);

        LOG.info("Created driver: {} with ID: {}", entity.getFirstName(), entity.getId());
        return entity;
    }

    @Transactional
    public Driver updateDriver(Long id, DriverDto driverDto) {
        return repo.findByIdOptional(id)
                .map(existing -> {
                    cacheInvalidationService.invalidateDriverCache(id);

                    existing.setFirstName(driverDto.getFirstName());
                    existing.setLastName(driverDto.getLastName());
                    existing.setEmail(driverDto.getEmail());
                    existing.setDrivingLicense(driverDto.getDrivingLicense());
                    existing.setUpdatedAt(Instant.now());
                    return existing;
                })
                .orElseThrow(
                    () -> new NotFoundException("Driver not found with id: " + id)
                );
    }

    @Transactional
    public void deleteDriver(Long id) {
        repo.findByIdOptional(id)
                .filter(r -> tripRepo.countByDriver(r.getId()) == 0)
                .filter(r -> carRepo.countByDriver(r.getId()) == 0)
                .ifPresentOrElse(r -> {
                    repo.deleteById(r.getId());
                    cacheInvalidationService.invalidateDriverCache(id);
                    LOG.info("Deleted driver with ID: {}", r.getId());
                }, () -> {
                    LOG.info("Driver with ID: {} not exists or not eligible for for deletion.", id);
                });
    }
}