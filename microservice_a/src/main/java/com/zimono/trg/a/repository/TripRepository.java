package com.zimono.trg.a.repository;

import com.zimono.trg.a.model.Trip;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@ApplicationScoped
public class TripRepository implements PanacheRepository<Trip> {
    private static final Logger LOG = LoggerFactory.getLogger(TripRepository.class);

    public List<Trip> findByCarAndByDriverAndEndTimeIsNull(Long carId, Long driverId) {
        return find("car.id = ?1 and driver.id = ?2 and endTime is null", carId, driverId).list();
    }

    //TODO:
    public List<Trip> findAllFull() {
        return find("SELECT t FROM Trip t LEFT JOIN FETCH t.car LEFT JOIN FETCH t.driver")
                .list();
    }

    //TODO:
    public Trip findByIdFull(Long id) {
        return find("SELECT t FROM Trip t LEFT JOIN FETCH t.car LEFT JOIN FETCH t.driver WHERE t.id = ?1", id)
                .firstResult();
    }

    public void deleteAllOfDriver(Long driverId) {
        LOG.info("Deleting all trips of driver id: {}", driverId);
        delete("driver.id = ?1", driverId);
    }


    public long  countByDriver(Long driverId) {
        return count("driver.id = ?1", driverId);
    }

    public long  countByCar(Long carId) {
        return count("car.id = ?1", carId);
    }
}
