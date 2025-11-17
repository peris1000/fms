package com.zimono.trg.a.service;

import com.zimono.trg.a.dto.CarAssignmentRequest;
import com.zimono.trg.a.dto.CarDto;
import com.zimono.trg.a.model.Car;
import com.zimono.trg.a.model.Driver;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CarServiceIT {

    @Inject
    CarService carService;

    @Inject
    DriverService driverService;

    private static Long testCarId;
    private static Long testDriverId;

    @Test
    @Order(1)
    @Transactional
    public void test_create_car() {
        CarDto carDto = new CarDto();
        carDto.setBrand("Tesla");
        carDto.setModel("Model S");
        carDto.setSerialNumber("TESLA12345");
        carDto.setLicensePlate("TEST123");

        Car created = carService.createCar(carDto);

        assertNotNull(created);
        assertEquals("Tesla", created.getBrand());
        assertEquals("Model S", created.getModel());
        assertEquals("TEST123", created.getLicensePlate());

        testCarId = created.getId();
    }

    @Test
    @Order(2)
    public void test_get_car_by_id() {
        Car car = carService.getCarById(testCarId);

        assertNotNull(car);
        assertEquals(testCarId, car.getId());
        assertEquals("Tesla", car.getBrand());
    }

    @Test
    @Order(3)
    public void test_get_all_cars() {
        var cars = carService.getAllCars();
        assertFalse(cars.isEmpty());
        assertTrue(cars.stream().anyMatch(c -> c.getId() == testCarId));
    }

    @Test
    @Order(4)
    @Transactional
    public void test_update_car() {
        CarDto updateDto = new CarDto();
        updateDto.setBrand("Tesla Updated");
        updateDto.setModel("Model X");
        updateDto.setSerialNumber("TESLA12345");
        updateDto.setLicensePlate("TEST123");

        Car updated = carService.update(testCarId, updateDto);

        assertNotNull(updated);
        assertEquals("Tesla Updated", updated.getBrand());
        assertEquals("Model X", updated.getModel());
    }

    @Test
    @Order(5)
    @Transactional
    public void test_create_car_with_duplicate_license_plate() {
        CarDto carDto = new CarDto();
        carDto.setBrand("BMW");
        carDto.setModel("X5");
        carDto.setSerialNumber("BMW12345");
        carDto.setLicensePlate("TEST123"); // Same as existing

        assertThrows(IllegalArgumentException.class, () -> carService.createCar(carDto));
    }

    @Test
    @Order(6)
    @Transactional
    public void test_assign_driver_to_car() {
        // Create a driver first
        var driverDto = new com.zimono.trg.a.dto.DriverDto();
        driverDto.setFirstName("John");
        driverDto.setLastName("Doe");
        driverDto.setEmail("john.doe@test.com");
        driverDto.setDrivingLicense("DL123456");

        Driver driver = driverService.createDriver(driverDto);
        testDriverId = driver.getId();

        CarAssignmentRequest request = new CarAssignmentRequest();
        request.carId = testCarId;
        request.driverId = testDriverId;

        Car assignedCar = carService.assignDriverToCar(request);

        assertNotNull(assignedCar);
        assertNotNull(assignedCar.getAssignedDriver());
        assertEquals(testDriverId, assignedCar.getAssignedDriver().getId());
    }

    @Test
    @Order(7)
    @Transactional
    public void test_unassign_driver_from_car() {
        Car unassignedCar = carService.unassignDriverFromCar(testCarId);

        assertNotNull(unassignedCar);
        assertNull(unassignedCar.getAssignedDriver());
    }

    @Test
    @Order(8)
    public void test_get_car_by_id_not_found() {
        assertThrows(NotFoundException.class, () -> carService.getCarById(99999L));
    }

    @Test
    @Order(9)
    @Transactional
    public void test_delete_car() {
        carService.delete(testCarId);
        assertThrows(NotFoundException.class, () -> carService.getCarById(testCarId));
    }
}
