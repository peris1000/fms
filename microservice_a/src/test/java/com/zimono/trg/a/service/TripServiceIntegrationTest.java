package com.zimono.trg.a.service;

import com.zimono.trg.a.dto.CarAssignmentRequest;
import com.zimono.trg.a.dto.CarDto;
import com.zimono.trg.a.dto.DriverDto;
import com.zimono.trg.a.dto.TripDto;
import com.zimono.trg.a.model.Car;
import com.zimono.trg.a.model.Driver;
import com.zimono.trg.a.model.Trip;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class TripServiceIntegrationTest {

    @Inject
    TripService tripService;

    @Inject
    CarService carService;

    @Inject
    DriverService driverService;

    private Long testCarId;
    private Long testDriverId;

    @BeforeEach
    public void setupTestData() {
        String unique = String.valueOf(System.nanoTime());
        // Create driver
        DriverDto driverDto = new DriverDto();
        driverDto.setFirstName("Jane");
        driverDto.setLastName("Smith");
        driverDto.setEmail("jane.smith+" + unique + "@test.com");
        driverDto.setDrivingLicense("DL" + unique);
        Driver driver = driverService.createDriver(driverDto);
        testDriverId = driver.getId();

        // Create car
        CarDto carDto = new CarDto();
        carDto.setBrand("BMW");
        carDto.setModel("X5");
        carDto.setSerialNumber("SN" + unique);
        carDto.setLicensePlate("TRIP" + unique);
        Car car = carService.createCar(carDto);
        testCarId = car.getId();

        // Assign driver to car
        CarAssignmentRequest assignmentRequest = new CarAssignmentRequest();
        assignmentRequest.carId = testCarId;
        assignmentRequest.driverId = testDriverId;
        carService.assignDriverToCar(assignmentRequest);
    }

    @Test
    public void test_create_trip() {
        TripDto tripDto = new TripDto();
        tripDto.setCarId(testCarId);
        tripDto.setDriverId(testDriverId);
        tripDto.setPlannedStartTime(LocalDateTime.now().plusHours(1));
        tripDto.setPlannedEndTime(LocalDateTime.now().plusHours(3));

        Trip created = tripService.createTrip(tripDto);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(testCarId, created.getCar().getId());
        assertEquals(testDriverId, created.getDriver().getId());
        assertNull(created.getStartTime());
        assertNull(created.getEndTime());
    }

    @Test
    public void test_get_trip_by_id() {
        // create a trip first
        TripDto tripDto = new TripDto();
        tripDto.setCarId(testCarId);
        tripDto.setDriverId(testDriverId);
        Trip created = tripService.createTrip(tripDto);

        Trip trip = tripService.getTripById(created.getId());

        assertNotNull(trip);
        assertEquals(created.getId(), trip.getId());
        assertEquals(testCarId, trip.getCar().getId());
    }

    @Test
    public void test_get_all_trips() {
        // create a trip to ensure at least one exists
        TripDto tripDto = new TripDto();
        tripDto.setCarId(testCarId);
        tripDto.setDriverId(testDriverId);
        Trip created = tripService.createTrip(tripDto);

        var trips = tripService.getAllTrips();
        assertFalse(trips.isEmpty());
        assertTrue(trips.stream().anyMatch(t -> Objects.equals(t.getId(), created.getId())));
    }

    @Test
    public void test_update_trip() {
        // create a trip first
        TripDto tripDto = new TripDto();
        tripDto.setCarId(testCarId);
        tripDto.setDriverId(testDriverId);
        Trip created = tripService.createTrip(tripDto);

        TripDto updateDto = new TripDto();
        updateDto.setCarId(testCarId);
        updateDto.setDriverId(testDriverId);
        updateDto.setPlannedStartTime(LocalDateTime.now().plusHours(2));
        updateDto.setPlannedEndTime(LocalDateTime.now().plusHours(5));

        Trip updated = tripService.update(created.getId(), updateDto);

        assertNotNull(updated);
        assertNotNull(updated.getPlannedStartTime());
        assertNotNull(updated.getPlannedEndTime());
    }

    @Test
    public void test_start_trip() {
        TripDto tripDto = new TripDto();
        tripDto.setCarId(testCarId);
        tripDto.setDriverId(testDriverId);
        Trip created = tripService.createTrip(tripDto);

        Trip started = tripService.startTrip(created.getId());

        assertNotNull(started);
        assertNotNull(started.getStartTime());
        assertNull(started.getEndTime());
    }

    @Test
    public void test_start_trip_already_started() {
        TripDto tripDto = new TripDto();
        tripDto.setCarId(testCarId);
        tripDto.setDriverId(testDriverId);
        Trip created = tripService.createTrip(tripDto);

        tripService.startTrip(created.getId());
        assertThrows(IllegalArgumentException.class, () -> tripService.startTrip(created.getId()));
    }

    @Test
    public void test_stop_trip() {
        TripDto tripDto = new TripDto();
        tripDto.setCarId(testCarId);
        tripDto.setDriverId(testDriverId);
        Trip created = tripService.createTrip(tripDto);

        tripService.startTrip(created.getId());
        Trip stopped = tripService.stopTrip(created.getId());

        assertNotNull(stopped);
        assertNotNull(stopped.getStartTime());
        assertNotNull(stopped.getEndTime());
    }

    @Test
    public void test_stop_trip_already_stopped() {
        TripDto tripDto = new TripDto();
        tripDto.setCarId(testCarId);
        tripDto.setDriverId(testDriverId);
        Trip created = tripService.createTrip(tripDto);

        tripService.startTrip(created.getId());
        tripService.stopTrip(created.getId());
        assertThrows(IllegalArgumentException.class, () -> tripService.stopTrip(created.getId()));
    }

    @Test
    public void test_create_trip_with_invalid_time_range() {
        TripDto tripDto = new TripDto();
        tripDto.setCarId(testCarId);
        tripDto.setDriverId(testDriverId);
        tripDto.setPlannedStartTime(LocalDateTime.now().plusHours(5));
        tripDto.setPlannedEndTime(LocalDateTime.now().plusHours(1)); // End before start

        assertThrows(IllegalArgumentException.class, () -> tripService.createTrip(tripDto));
    }

    @Test
    public void test_get_trip_by_id_not_found() {
        assertThrows(NotFoundException.class, () -> tripService.getTripById(99999L));
    }

    @Test
    public void test_delete_trip() {
        TripDto tripDto = new TripDto();
        tripDto.setCarId(testCarId);
        tripDto.setDriverId(testDriverId);
        Trip created = tripService.createTrip(tripDto);

        tripService.delete(created.getId());
        assertThrows(NotFoundException.class, () -> tripService.getTripById(created.getId()));
    }
}
