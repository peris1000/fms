package com.zimono.trg.c.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "penalty")
public class DriverPenalty implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    private long id;

    @Column(name = "trip_id")
    @NotNull(message = "Trip id cannot be null")
    private long tripId;

    @Column(name = "car_id")
    @NotNull(message = "Car id cannot be null")
    private long carId;

    @Column(name = "driver_id")
    @NotNull(message = "Driver id cannot be null")
    private long driverId;

    @Column(name = "penalty_points")
    private int penaltyPoints = 0;

    @Column(name = "created_at")
    private Instant createdAt;

    public DriverPenalty() {}

    public DriverPenalty(long tripId, long carId, long driverId, int penaltyPoints) {
        this.tripId = tripId;
        this.carId = carId;
        this.driverId = driverId;
        this.penaltyPoints = penaltyPoints;
        this.createdAt = Instant.now();
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public long getTripId() {
        return tripId;
    }
    public void setTripId(long tripId) {
        this.tripId = tripId;
    }

    public long getCarId() {
        return carId;
    }
    public void setCarId(long carId) {
        this.carId = carId;
    }

    public long getDriverId() {
        return driverId;
    }
    public void setDriverId(long driverId) {
        this.driverId = driverId;
    }

    public int getPenaltyPoints() {
        return penaltyPoints;
    }
    public void setPenaltyPoints(int penaltyPoints) {
        this.penaltyPoints = penaltyPoints;
    }
    public void addPenaltyPoints(Integer points) {
        this.penaltyPoints += points;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public DriverPenalty combine(DriverPenalty other) {
        return new DriverPenalty(
                this.tripId,
                this.carId,
                this.driverId,
                this.penaltyPoints + other.getPenaltyPoints()
        );
    }

}
