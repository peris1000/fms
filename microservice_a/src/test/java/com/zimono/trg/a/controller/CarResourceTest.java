package com.zimono.trg.a.controller;

import com.zimono.trg.a.dto.CarAssignmentRequest;
import com.zimono.trg.a.dto.CarDto;
import com.zimono.trg.a.dto.DriverDto;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CarResourceTest {


    private static Long carId;
    private static Long driverId;

    @Test
    @Order(1)
    public void test_create_car() {
        CarDto carDto = new CarDto();
        carDto.setBrand("Audi");
        carDto.setModel("A4");
        carDto.setSerialNumber("AUDI12345");
        carDto.setLicensePlate("CAR001");

        Integer carIdInt = given()

                .contentType(ContentType.JSON)
                .body(carDto)
                .when()
                .post("/api/cars")
                .then()
                .statusCode(201)
                .body("brand", equalTo("Audi"))
                .body("model", equalTo("A4"))
                .body("license_plate", equalTo("CAR001"))
                .extract()
                .path("id");
        carId = carIdInt.longValue();
    }

    @Test
    @Order(2)
    public void test_get_all_cars() {
        given()
                
                .when()
                .get("/api/cars")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    @Order(3)
    public void test_get_car_by_id() {
        given()
                
                .when()
                .get("/api/cars/" + carId)
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("brand", equalTo("Audi"));
    }

    @Test
    @Order(4)
    public void test_update_car() {
        CarDto updateDto = new CarDto();
        updateDto.setBrand("Audi");
        updateDto.setModel("A6");
        updateDto.setSerialNumber("AUDI12345");
        updateDto.setLicensePlate("CAR001");

        given()

                .contentType(ContentType.JSON)
                .body(updateDto)
                .when()
                .put("/api/cars/" + carId)
                .then()
                .statusCode(200)
                .body("model", equalTo("A6"));
    }

    @Test
    @Order(5)
    public void test_create_car_with_invalid_license_plate() {
        CarDto carDto = new CarDto();
        carDto.setBrand("Ford");
        carDto.setModel("Focus");
        carDto.setSerialNumber("FORD12345");
        carDto.setLicensePlate("ABC"); // Too short

        given()

                .contentType(ContentType.JSON)
                .body(carDto)
                .when()
                .post("/api/cars")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(6)
    public void test_assign_driver_to_car() {
        // Create driver first
        DriverDto driverDto = new DriverDto();
        driverDto.setFirstName("Alice");
        driverDto.setLastName("Johnson");
        driverDto.setEmail("alice.j@test.com");
        driverDto.setDrivingLicense("DL111222");

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
                .statusCode(200)
                .body("assigned_driver_id", equalTo(driverId.intValue()));
    }

    @Test
    @Order(7)
    public void test_unassign_driver_from_car() {
        given()

                .when()
                .header("Content-Type", "application/json")
                .post("/api/cars/" + carId + "/unassign-driver")
                .then()
                .statusCode(200)
                .body("assigned_driver_id", nullValue());
    }

    @Test
    @Order(8)
    public void test_get_car_by_id_not_found() {
        given()

                .when()
                .get("/api/cars/99999")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(9)
    public void test_delete_car() {
        given()

                .when()
                .delete("/api/cars/" + carId)
                .then()
                .statusCode(204);

        // Verify deletion
        given()

                .when()
                .get("/api/cars/" + carId)
                .then()
                .statusCode(404);
    }
}
