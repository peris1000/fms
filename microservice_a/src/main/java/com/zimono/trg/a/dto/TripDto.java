package com.zimono.trg.a.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.zimono.trg.a.model.Trip;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "car_id", "driver_id", "planned_start_time", "planned_end_time", "start_time", "end_time" })
public class TripDto {
    @JsonProperty("id")
    private Long id;
    @NotNull(message = "Driver id is required")
    @JsonProperty("driver_id")
    private Long driverId;
    @NotNull(message = "Car id is required")
    @JsonProperty("car_id")
    private Long carId;
    @JsonProperty("planned_start_time")
    private LocalDateTime plannedStartTime;
    @JsonProperty("planned_end_time")
    private LocalDateTime plannedEndTime;
    @JsonProperty("start_time")
    private LocalDateTime startTime;
    @JsonProperty("end_time")
    private LocalDateTime endTime;

    public TripDto() {}

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Long getDriverId() {
        return driverId;
    }
    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public Long getCarId() {
        return carId;
    }
    public void setCarId(Long carId) {
        this.carId = carId;
    }

    public LocalDateTime getPlannedStartTime() {
        return plannedStartTime;
    }
    public void setPlannedStartTime(LocalDateTime plannedStartTime) {
        this.plannedStartTime = plannedStartTime;
    }

    public LocalDateTime getPlannedEndTime() {
        return plannedEndTime;
    }
    public void setPlannedEndTime(LocalDateTime plannedEndTime) {
        this.plannedEndTime = plannedEndTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public static TripDto fromEntity(Trip trip) {
        TripDto dto = new TripDto();
        dto.id = trip.getId();
        dto.setCarId(trip.getCar().getId());
        dto.setDriverId(trip.getDriver().getId());
        dto.setPlannedStartTime(trip.getPlannedStartTime());
        dto.setPlannedEndTime(trip.getPlannedEndTime());
        dto.setStartTime(trip.getStartTime());
        dto.setEndTime(trip.getEndTime());
        return dto;
    }
}
