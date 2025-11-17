package com.zimono.trg.a.dto;

public record CarSearchRequest(String brand,
                               String model,
                               String serialNumber,
                               String licensePlate,
                               Long assignedDriverId) {

}
