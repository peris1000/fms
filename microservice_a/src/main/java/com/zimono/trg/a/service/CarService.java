package com.zimono.trg.a.service;

import com.zimono.trg.a.dto.*;
import com.zimono.trg.a.model.Car;
import com.zimono.trg.a.model.Driver;
import com.zimono.trg.a.repository.CarRepository;
import com.zimono.trg.a.repository.DriverRepository;
import com.zimono.trg.a.repository.TripRepository;
import io.quarkus.cache.CacheResult;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
public class CarService {
    private static final Logger LOG = LoggerFactory.getLogger(CarService.class.getName());

    @Inject
    CarRepository carRepo;
    @Inject
    DriverRepository driverRepo;
    @Inject
    TripRepository tripRepo;
    @Inject
    CacheInvalidationService cacheInvalidationService;

    public List<Car> getAllCars() {
        return carRepo.listAll();
    }

    public PaginatedResponse<Car> search(CarSearchRequest request, int page, int size, Sort sortConfig) {
        var query = carRepo.search(request, sortConfig);
        long total = query.count();
        var cars = query
                .page(Page.of(page, size))
                .list();
        return new PaginatedResponse<>(page, size, total, cars);
    }

    @CacheResult(cacheName = "cars-cache")
    public Car getCarById(Long id) {
        return carRepo.findByIdOptional(id).orElseThrow(
            () -> new NotFoundException("Car not found with id: " + id)
        );
    }


    @Transactional
    public Car createCar(CarDto dto) {
        return (Car) carRepo.findByLicensePlateNumberOptional(dto.getLicensePlate())
                .map(car -> {
                    throw new IllegalArgumentException("Car " + car.getId() + " has the same license plate");
                }).orElseGet(() -> {
                    Car entity = new Car();
                    entity.setBrand(dto.getBrand());
                    entity.setModel(dto.getModel());
                    entity.setSerialNumber(dto.getSerialNumber());
                    entity.setLicensePlate(dto.getLicensePlate());
                    entity.setCreatedAt(Instant.now());
                    entity.setUpdatedAt(Instant.now());
                    carRepo.persist(entity);
                    return entity;
                });
    }

    @Transactional
    public Car update(Long carId, CarDto carDto) {
        // validate that no other car has the same license plate
        carRepo.findByLicensePlateNumberOptional(carDto.getLicensePlate()).ifPresent(car -> {
            if (!Objects.equals(car.getId(), carId)) {
                throw new IllegalArgumentException("Car " + car.getId() + " has the same license plate");
            }
        });

        Car existing = carRepo.findById(carId);
        if (existing == null) {
            throw new NotFoundException("Car not found with id: " + carId);
        }

        cacheInvalidationService.invalidateCarCache(existing.getId());

        existing.setBrand(carDto.getBrand());
        existing.setModel(carDto.getModel());
        existing.setSerialNumber(carDto.getSerialNumber());
        existing.setLicensePlate(carDto.getLicensePlate());
        existing.setUpdatedAt(Instant.now());

        LOG.info("Updated car with ID: {}", carId);
        return existing;
    }

    @Transactional
    public void delete(Long id) {
        Car existing = Optional.ofNullable(carRepo.findById(id))
                .map(car -> {
                    if (tripRepo.countByCar(id) > 0) {
                        throw new IllegalArgumentException("Car has trips assigned. Cannot delete.");
                    }
                    return car;
                })
                .orElseThrow(() -> new NotFoundException("Car not found with id: " + id));

        carRepo.deleteById(id);

        // invalidate cache
        cacheInvalidationService.invalidateCarCache(existing.getId());
        LOG.info("Deleted car with ID: {}", id);
    }

    @Transactional
    public Car assignDriverToCar(@Valid CarAssignmentRequest request) {
        Car car = carRepo.findById(request.carId);
        Driver driver = driverRepo.findById(request.driverId);

        if (car == null || driver == null) {
            throw new NotFoundException("Car or Driver not found");
        }
        if (car.getAssignedDriver() != null) {
            throw new NotFoundException("Car already assigned to a driver");
        }

        car.setAssignedDriver(driver);
        car.setUpdatedAt(Instant.now());
        carRepo.persist(car);

        // Invalidate cache
        cacheInvalidationService.invalidateCarCache(car.getId());
        cacheInvalidationService.invalidateDriverCache(driver.getId());
        return car;
    }

    @Transactional
    public Car unassignDriverFromCar(Long carId) {
        Car car = carRepo.findById(carId);
        if (car == null) {
            throw new NotFoundException("Car not found with id: " + carId);
        }
        if (car.getAssignedDriver() == null) {
            return car;
        }

        Long driverId = car.getAssignedDriver() != null ? car.getAssignedDriver().getId() : null;
        car.setAssignedDriver(null);
        car.setUpdatedAt(Instant.now());
        carRepo.persist(car);

        // Invalidate cache
        cacheInvalidationService.invalidateCarCache(carId);
        if (driverId != null) {
            cacheInvalidationService.invalidateDriverCache(driverId);
        }
        return car;
    }

}
