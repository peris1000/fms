package com.zimono.trg.a.service;

import com.zimono.trg.a.dto.TripDto;
import com.zimono.trg.a.model.Car;
import com.zimono.trg.a.model.Driver;
import com.zimono.trg.a.model.Trip;
import com.zimono.trg.a.producer.TripProducer;
import com.zimono.trg.a.repository.DriverRepository;
import com.zimono.trg.a.repository.TripRepository;
import com.zimono.trg.shared.TripMessage;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class TripService {
    private static final Logger LOG = LoggerFactory.getLogger(TripService.class.getName());

    @Inject
    TripRepository repo;
    @Inject
    TripProducer tripProducer;
    @Inject
    CacheInvalidationService cacheInvalidationService;
    @Inject
    CarService carService;
    @Inject
    DriverRepository driverRepo;

    public List<Trip> getAllTrips() {
        return repo.listAll();
    }

    @CacheResult(cacheName = "trips-cache")
    public Trip getTripById(Long id) {
        Trip entity = repo.findByIdOptional(id).orElse(null);
        if (entity == null) {
            throw new NotFoundException("Trip not found with id: " + id);
        }
        return entity;
    }

    @Transactional
    public Trip createTrip(TripDto dto) {

        Car car = carService.getCarById(dto.getCarId());
        if (car == null) {
            throw new NotFoundException("Car not found with id: " + dto.getCarId());
        }
        if (car.getAssignedDriver() == null) {
            throw new IllegalArgumentException("You need to assign driver first to the car with id: " + dto.getCarId());
        } else {
            if (!Objects.equals(car.getAssignedDriver().getId(), dto.getDriverId())) {
                throw new IllegalArgumentException("The car " + dto.getCarId() +
                        " is already assigned to a different driver " + car.getAssignedDriver().getId());
            }
        }

        if (dto.getPlannedStartTime() != null && dto.getPlannedEndTime() != null) {
            if (dto.getPlannedStartTime().isAfter(dto.getPlannedEndTime())) {
                throw new IllegalArgumentException("Start time must be before end time");
            }
        }

        Driver driver = car.getAssignedDriver();
        List<Trip> pendingTrips = repo.findByCarAndByDriverAndEndTimeIsNull(car.getId(), driver.getId());
        if (!pendingTrips.isEmpty()) {
            throw new IllegalArgumentException("You already have a pending trip for this car");
        }

        Trip entity = new Trip();
        entity.setCar(car);
        entity.setDriver(driver);
        entity.setPlannedStartTime(dto.getPlannedStartTime());
        entity.setPlannedEndTime(dto.getPlannedEndTime());
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        repo.persist(entity);

        // invalidate cache
        cacheInvalidationService.invalidateTripCache(entity.getId());
        LOG.info("Created trip with ID: {}", entity.getId());
        return entity;
    }

    @Transactional
    public Trip update(Long id, TripDto dto) {
        Trip existing = getTripById(id);
        if (existing != null) {
            if (existing.getStartTime() != null) {
                throw new IllegalArgumentException("Trip already started. Cannot update.");
            }
            Car car = carService.getCarById(dto.getCarId());
            existing.setCar(car);
            Driver driver = driverRepo.findById(dto.getDriverId());
            existing.setDriver(driver);
            existing.setPlannedStartTime(dto.getPlannedStartTime());
            existing.setPlannedEndTime(dto.getPlannedEndTime());
            existing.setUpdatedAt(Instant.now());

            // invalidate cache
            cacheInvalidationService.invalidateTripCache(id);
            LOG.info("Updated trip with ID: {}", id);
            return existing;
        } else {
            throw new NotFoundException("Trip not found");
        }
    }

    @Transactional
    public void delete(Long id) {
        Trip existing = getTripById(id);
        if (existing != null) {
            repo.deleteById(id);
            // invalidate cache
            cacheInvalidationService.invalidateTripCache(id);
            LOG.info("Deleted trip with ID: {}", id);
        } else {
            throw new NotFoundException("Trip not found");
        }
    }

    @Transactional
    public Trip startTrip(Long id) {
        Trip existing = getTripById(id);
        if (existing != null) {
            if (existing.getEndTime() != null) {
                throw new IllegalArgumentException("Trip already ended. You better create a new one.");
            }
            if (existing.getStartTime() != null) {
                throw new IllegalArgumentException("Trip already started");
            }

            existing.setStartTime(LocalDateTime.now());

            // send message to kafka
            tripProducer.startTrip(new TripMessage.Builder()
                    .tripId(existing.getId())
                    .carId(existing.getCar().getId())
                    .driverId(existing.getDriver().getId()).build()
            );
            // invalidate cache
            cacheInvalidationService.invalidateTripCache(id);
            LOG.info("Starting trip with ID: {}", id);
            return existing;
        } else {
            throw new NotFoundException("Trip not found");
        }
    }

    @Transactional
    public Trip stopTrip(Long id) {
        Trip existing = getTripById(id);
        if (existing != null) {
            if (existing.getStartTime() == null) {
                throw new IllegalArgumentException("Trip not started yet");
            }
            if (existing.getEndTime() != null) {
                throw new IllegalArgumentException("Trip already ended");
            }
            existing.setEndTime(LocalDateTime.now());

            // send message to kafka
            tripProducer.stopTrip(new TripMessage.Builder()
                    .tripId(existing.getId())
                    .carId(existing.getCar().getId())
                    .driverId(existing.getDriver().getId()).build()
            );

            // invalidate cache
            cacheInvalidationService.invalidateTripCache(id);
            LOG.info("Stopping trip with ID: {}", id);
            return existing;
        } else {
            throw new NotFoundException("Trip not found");
        }
    }
}
