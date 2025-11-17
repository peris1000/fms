package com.zimono.trg.a.service;

import com.zimono.trg.a.dto.DriverDto;
import com.zimono.trg.a.model.Driver;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DriverServiceTest {

    @Inject
    DriverService driverService;

    private static Long driverId;

    @Test
    @Order(1)
    @Transactional
    public void test_create_driver() {
        DriverDto driverDto = new DriverDto();
        driverDto.setFirstName("Test First");
        driverDto.setLastName("Test Last");
        driverDto.setDrivingLicense("TEST123");
        driverDto.setEmail("test@example.com");

        Driver created = driverService.createDriver(driverDto);
        assertEquals("Test Last", created.getLastName());
        driverId = created.getId();
    }

    @Test
    @Order(2)
    public void test_get_driver_by_id() {
        Driver driver = driverService.getDriverById(driverId);
        assertNotNull(driver);
        assertEquals("Test Last", driver.getLastName());
    }

    @Test
    @Order(3)
    public void test_get_all_drivers() {
        List<Driver> drivers = driverService.getAllDrivers();
        assertFalse(drivers.isEmpty());
    }
}