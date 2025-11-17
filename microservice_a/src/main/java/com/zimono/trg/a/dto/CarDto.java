package com.zimono.trg.a.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.zimono.trg.a.model.Car;
import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "brand", "model", "serial_number", "license_plate", "assigned_driver_id" })
public class CarDto {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("brand")
    private String brand;

    @JsonProperty("model")
    private String model;

    @JsonProperty("serial_number")
    private String serialNumber;

    @NotBlank(message = "License plate is required")
    @Size(min = 4, max = 8, message = "License plates must be between 4 and 8 alphas.")
    @JsonProperty("license_plate")
    private String licensePlate;

    @JsonProperty("assigned_driver_id")
    private Long assignedDriverId;

    public CarDto() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Long getAssignedDriverId() {
        return assignedDriverId;
    }

    public void setAssignedDriverId(Long assignedDriverId) {
        this.assignedDriverId = assignedDriverId;
    }

    public static CarDto fromEntity(Car car) {
        CarDto dto = new CarDto();
        dto.setId(car.getId());
        dto.setLicensePlate(car.getLicensePlate());
        dto.setModel(car.getModel());
        dto.setBrand(car.getBrand());
        dto.setSerialNumber(car.getSerialNumber());
        dto.setAssignedDriverId(car.getAssignedDriver() != null ? car.getAssignedDriver().getId() : null);
        return dto;
    }
}
