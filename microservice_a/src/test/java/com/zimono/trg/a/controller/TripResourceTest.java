package com.zimono.trg.a.controller;

import com.zimono.trg.a.dto.CarAssignmentRequest;
import com.zimono.trg.a.dto.CarDto;
import com.zimono.trg.a.dto.DriverDto;
import com.zimono.trg.a.dto.TripDto;
import com.zimono.trg.a.producer.TripProducer;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TripResourceTest {

    @TestHTTPResource
    URI baseUri;

    private static Long tripId;
    private static Long carId;
    private static Long driverId;

    @InjectMock
    TripProducer tripProducer;

    @BeforeAll
    public void setupTestData() {
        // while we are in @BeforeAll make sure restAssured is configured to use our baseUri
        RestAssured.baseURI = baseUri.toString();

        // Create driver
        DriverDto driverDto = new DriverDto();
        driverDto.setFirstName("Trip");
        driverDto.setLastName("Driver");
        driverDto.setEmail("trip.driver@test.com");
        driverDto.setDrivingLicense("DL555666");

        Integer driverIdInt = given()
                .contentType(ContentType.JSON)
                .body(driverDto)
                .when()
                .post("/api/drivers")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
        driverId = driverIdInt.longValue();

        // Create car
        CarDto carDto = new CarDto();
        carDto.setBrand("Mercedes");
        carDto.setModel("C-Class");
        carDto.setSerialNumber("MERC12345");
        carDto.setLicensePlate("TRIP123");

        Integer carIdInt = given()
                .contentType(ContentType.JSON)
                .body(carDto)
                .when()
                .post("/api/cars")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
        carId = carIdInt.longValue();

        // Assign driver to car
        CarAssignmentRequest request = new CarAssignmentRequest();
        request.carId = carId;
        request.driverId = driverId;

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/cars/assign-driver")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(1)
    public void test_create_trip() {
        TripDto tripDto = new TripDto();
        tripDto.setCarId(carId);
        tripDto.setDriverId(driverId);
        tripDto.setPlannedStartTime(LocalDateTime.now().plusHours(1));
        tripDto.setPlannedEndTime(LocalDateTime.now().plusHours(3));

        Integer tripIdInt = given()
                .contentType(ContentType.JSON)
                .body(tripDto)
                .when()
                .post("/api/trips")
                .then()
                .statusCode(201)
                .body("car_id", equalTo(carId.intValue()))
                .body("driver_id", equalTo(driverId.intValue()))
                .extract()
                .path("id");
        tripId = tripIdInt.longValue();
    }

    @Test
    @Order(2)
    public void test_get_all_trips() {
        given()
                .when()
                .get("/api/trips")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    @Order(3)
    public void test_get_trip_by_id() {
        given()
                .when()
                .get("/api/trips/" + tripId)
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("car_id", equalTo(carId.intValue()));
    }

    @Test
    @Order(4)
    public void test_start_trip() {
        doNothing().when(tripProducer).startTrip(any());

        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/api/trips/" + tripId + "/start")
                .then()
                .statusCode(200)
                .body("start_time", notNullValue());
    }

    @Test
    @Order(5)
    public void test_stop_trip() {
        doNothing().when(tripProducer).stopTrip(any());

        // Ensure the trip is started (idempotent: 200 if started now, or 409 if already started)
        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/api/trips/" + tripId + "/start")
                .then()
                .statusCode(anyOf(is(200), is(409)));

        // Now stop the trip
        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/api/trips/" + tripId + "/stop")
                .then()
                .statusCode(200)
                .body("start_time", notNullValue())
                .body("end_time", notNullValue());
    }

    @Test
    @Order(6)
    public void test_get_trip_by_id__not_found() {
        given()
                .when()
                .get("/api/trips/99999")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(7)
    public void test_delete_trip() {
        given()
                .when()
                .delete("/api/trips/" + tripId)
                .then()
                .statusCode(204);

        // Verify deletion
        given()
                .when()
                .get("/api/trips/" + tripId)
                .then()
                .statusCode(404);
    }
}
