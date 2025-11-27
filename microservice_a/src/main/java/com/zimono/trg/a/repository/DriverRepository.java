package com.zimono.trg.a.repository;

import com.zimono.trg.a.model.Driver;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.Map;

@ApplicationScoped
public class DriverRepository implements PanacheRepository<Driver> {

    public int updateDriverPenalties(long driverId, int penaltyPoints, Instant time) {

        return update("updatedAt=:time, penaltyPoints = penaltyPoints + :penaltyPoints where id= :driverId",
                Map.of("driverId", driverId, "penaltyPoints", penaltyPoints, "time", time));
    }

}
