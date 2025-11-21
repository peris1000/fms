package com.zimono.trg.a.service;


import com.zimono.trg.a.dto.DriverDto;
import com.zimono.trg.a.model.Driver;
import com.zimono.trg.a.producer.TripProducer;
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
    @Inject
    TripProducer tripProducer;


    public List<Driver> getAllDrivers() {
        return repo.listAll();
    }

    @CacheResult(cacheName = "drivers-cache")
    public Driver getDriverById(Long id) {
        Driver entity = repo.findById(id);
        if (entity == null) {
            throw new NotFoundException("Driver not found with id: " + id);
        }
        return entity;
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
        Driver entity = repo.findById(id);
        if (entity == null) {
            throw new NotFoundException("Driver not found with id: " + id);
        }

        // invalidate cache
        cacheInvalidationService.invalidateDriverCache(id);

        entity.setFirstName(driverDto.getFirstName());
        entity.setLastName(driverDto.getLastName());
        entity.setEmail(driverDto.getEmail());
        entity.setDrivingLicense(driverDto.getDrivingLicense());
        entity.setUpdatedAt(Instant.now());

        LOG.info("Updated driver with ID: {}", id);
        return entity;
    }

    @Transactional
    public void deleteDriver(Long id) {
        Driver driver = repo.findById(id);
        if (driver == null) {
            throw new NotFoundException("Driver not found with id: " + id);
        }

        long tripCount = tripRepo.countByDriver(id);
        if (tripCount > 0) {
            throw new IllegalArgumentException("Driver has trips assigned. Cannot delete.");
        }
        long carCount = carRepo.countByDriver(id);
        if (carCount > 0) {
            throw new IllegalArgumentException("Driver has cars assigned. Cannot delete.");
        }
        repo.deleteById(id);

        // invalidate cache
        cacheInvalidationService.invalidateDriverCache(id);
        LOG.info("Deleted driver with ID: {}", id);
    }
}