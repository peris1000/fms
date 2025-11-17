package com.zimono.trg.a.controller;

import com.zimono.trg.a.dto.DriverDto;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DriverResourceTest {


    private static Long driverId;

    @Test
    @Order(1)
    public void test_create_driver() {
        DriverDto driverDto = new DriverDto();
        driverDto.setFirstName("Bob");
        driverDto.setLastName("Wilson");
        driverDto.setEmail("bob.wilson@test.com");
        driverDto.setDrivingLicense("DL333444");

        Integer driverIdInt = given()
                .contentType(ContentType.JSON)
                .body(driverDto)
                .when()
                .post("/api/drivers")
                .then()
                .statusCode(201)
                .body("first_name", equalTo("Bob"))
                .body("last_name", equalTo("Wilson"))
                .body("email", equalTo("bob.wilson@test.com"))
                .extract()
                .path("id");
        driverId = driverIdInt.longValue();
    }

    @Test
    @Order(2)
    public void test_get_all_drivers() {
        given()
                .when()
                .get("/api/drivers")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    @Order(3)
    public void test_get_driver_by_id() {
        given()
                
                .when()
                .get("/api/drivers/" + driverId)
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("first_name", equalTo("Bob"))
                .body("last_name", equalTo("Wilson"));
    }

    @Test
    @Order(4)
    public void test_update_driver() {
        DriverDto updateDto = new DriverDto();
        updateDto.setFirstName("Robert");
        updateDto.setLastName("Wilson");
        updateDto.setEmail("robert.wilson@test.com");
        updateDto.setDrivingLicense("DL333444");

        given()
                
                .contentType(ContentType.JSON)
                .body(updateDto)
                .when()
                .put("/api/drivers/" + driverId)
                .then()
                .statusCode(200)
                .body("first_name", equalTo("Robert"))
                .body("email", equalTo("robert.wilson@test.com"));
    }

    @Test
    @Order(5)
    public void test_create_driver_with_missing_license() {
        DriverDto driverDto = new DriverDto();
        driverDto.setFirstName("Charlie");
        driverDto.setLastName("Brown");
        driverDto.setEmail("charlie@test.com");
        // Missing driving license

        given()
                
                .contentType(ContentType.JSON)
                .body(driverDto)
                .when()
                .post("/api/drivers")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(6)
    public void test_get_driver_by_id__not_found() {
        given()
                
                .when()
                .get("/api/drivers/99999")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(7)
    public void test_delete_driver() {
        given()
                
                .when()
                .delete("/api/drivers/" + driverId)
                .then()
                .statusCode(204);

        // Verify deletion
        given()
                
                .when()
                .get("/api/drivers/" + driverId)
                .then()
                .statusCode(404);
    }
}
