package com.zimono.trg.a.repository;

import com.zimono.trg.a.dto.CarSearchRequest;
import com.zimono.trg.a.model.Car;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class CarRepository implements PanacheRepository<Car> {

    public Optional<Car> findByLicensePlateNumberOptional(String licensePlate) {
        return find("licensePlate", licensePlate).firstResultOptional();
    }

    public PanacheQuery<Car> search(CarSearchRequest req, Sort sort) {

        StringBuilder query = new StringBuilder();
        Map<String, Object> params = new HashMap<>();

        if (req.brand() != null && !req.brand().isBlank()) {
            query.append("lower(brand) like :brand");
            params.put("brand", "%" + req.brand().toLowerCase() + "%");
        }
        if (req.model() != null && !req.model().isBlank()) {
            appendAnd(query);
            query.append("lower(model) like :model");
            params.put("model", "%" + req.model().toLowerCase() + "%");
        }
        if (req.serialNumber() != null && !req.serialNumber().isBlank()) {
            appendAnd(query);
            query.append("lower(serialNumber) like :serialNumber");
            params.put("serialNumber", "%" + req.serialNumber().toLowerCase() + "%");
        }
        if (req.licensePlate() != null && !req.licensePlate().isBlank()) {
            appendAnd(query);
            query.append("lower(licensePlate) like :licensePlate");
            params.put("licensePlate", "%" + req.licensePlate().toLowerCase() + "%");
        }
        if (req.assignedDriverId() != null) {
            appendAnd(query);
            query.append("assignedDriver.id = :driverId");
            params.put("driverId", req.assignedDriverId());
        }

        if (query.isEmpty()) {
            return findAll(sort);
        }
        return find(query.toString(), sort, params);
    }

    private void appendAnd(StringBuilder query) {
        if (!query.isEmpty()) query.append(" and ");
    }

    public void unassignDriverFromCars(Long driverId) {
        update("assignedDriver = null where assignedDriver.id = :driverId", Map.of("driverId", driverId));
    }

    public int unassignDriverFromCar(Long carId) {
        return update("assignedDriver = null where id = :carId", Map.of("carId", carId));
    }

    public long  countByDriver(Long driverId) {
        return count("assignedDriver.id = ?1", driverId);
    }
}
