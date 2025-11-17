package com.zimono.trg.a;

import com.zimono.trg.a.dto.CarAssignmentRequest;
import com.zimono.trg.a.dto.CarDto;
import com.zimono.trg.a.dto.DriverDto;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestSecurity(user = "admin", roles = {"admin", "operator", "viewer"})
@OidcSecurity(claims = {
    @Claim(key = "email", value = "admin@fms.com"),
    @Claim(key = "preferred_username", value = "admin")
})
class CarDriverResourceTest {


    @Test
    public void test_create_and_get_driver() {
        DriverDto driver = new DriverDto();
        driver.setFirstName("John");
        driver.setLastName("Doe");
        driver.setDrivingLicense("LIC123");
        driver.setEmail("john@example.com");

        given()
                
                .contentType(ContentType.JSON)
                .body(driver)
                .when().post("/api/drivers")
                .then()
                .statusCode(201);

        given()
                
                .when().get("/api/drivers")
                .then()
                .statusCode(200);
    }

    @Test
    public void test_assign_driver_to_car() {
        // Create driver first
        DriverDto dto = new DriverDto();
        dto.setFirstName("Jane");
        dto.setLastName("Smith");
        dto.setDrivingLicense("LIC456");
        dto.setEmail("jane@example.com");

        Integer driverIdInt = given()
                
                .contentType(ContentType.JSON)
                .body(dto)
                .when().post("/api/drivers")
                .then()
                .statusCode(201)
                .extract().path("id");
        long driverId = driverIdInt.longValue();

        // Create car
        CarDto car = new CarDto();
        car.setBrand("Tesla");
        car.setModel("Model S");
        car.setSerialNumber("2025/0123456789");
        car.setLicensePlate("ABC123");

        Integer carIdInt = given()
                
                .contentType(ContentType.JSON)
                .body(car)
                .when().post("/api/cars")
                .then()
                .statusCode(201)
                .extract().path("id");
        long carId = carIdInt.longValue();

        // Test assignment
        CarAssignmentRequest assignment = new CarAssignmentRequest();
        assignment.driverId = driverId;
        assignment.carId = carId;

        given()
                
                .contentType(ContentType.JSON)
                .body(assignment)
                .when().post("/api/cars/assign-driver")
                .then()
                .statusCode(200);
    }

}