package com.zimono.trg.a.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class CarAssignmentRequest {
    @NotNull
    @JsonProperty("car_id")
    public Long carId;
    @JsonProperty("driver_id")
    @NotNull
    public Long driverId;
}