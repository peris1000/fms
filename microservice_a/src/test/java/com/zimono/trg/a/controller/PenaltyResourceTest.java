package com.zimono.trg.a.controller;

import com.zimono.trg.a.client.PenaltyClient;
import com.zimono.trg.a.model.Driver;
import com.zimono.trg.a.model.Trip;
import com.zimono.trg.a.service.DriverService;
import com.zimono.trg.a.service.TripService;
import com.zimono.trg.shared.net.DriverPenaltyDto;
import com.zimono.trg.shared.net.TripPenaltiesDto;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.http.ContentType;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;

import java.lang.annotation.Annotation;
import jakarta.enterprise.util.AnnotationLiteral;
import java.time.Instant;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestSecurity(user = "admin", roles = {"admin", "operator", "viewer"})
@OidcSecurity(claims = {
        @Claim(key = "email", value = "admin@fms.com"),
        @Claim(key = "preferred_username", value = "admin")
})
public class PenaltyResourceTest {

    @InjectMock
    DriverService driverService;

    @InjectMock
    TripService tripService;

    // RestClient beans are not eligible for @InjectMock, install via QuarkusMock with @RestClient qualifier
    PenaltyClient penaltyClient;

    @BeforeEach
    void setupRestClientMock() {
        penaltyClient = Mockito.mock(PenaltyClient.class);
        QuarkusMock.installMockForType(penaltyClient, PenaltyClient.class, new AnnotationLiteral<RestClient>(){});
    }

    @Test
    @TestSecurity(user = "admin", roles = {"admin"})
    public void test_get_all_penalties_as_admin() {
        // Arrange
        List<DriverPenaltyDto> payload = List.of(
                new DriverPenaltyDto(1L, 100L, 200L, 10L, 3, Instant.now()),
                new DriverPenaltyDto(2L, 101L, 201L, 11L, 5, Instant.now())
        );
        when(penaltyClient.getAllPenalties()).thenReturn(payload);

        // Act + Assert
        given()
                .when()
                .get("/api/penalties/")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(2))
                .body("[0].driver_penalty_id", equalTo(1))
                .body("[1].penalty_points", equalTo(5));
    }

    @Test
    @TestSecurity(user = "operator", roles = {"operator"})
    public void test_get_penalties_for_driver_as_operator() {
        // Arrange
        long driverId = 42L;
        Driver driver = new Driver();
        driver.setId(driverId);
        driver.setEmail("driver@test.com");
        when(driverService.getDriverById(driverId)).thenReturn(driver);

        List<DriverPenaltyDto> payload = List.of(
                new DriverPenaltyDto(3L, 700L, 800L, driverId, 2, Instant.now())
        );
        when(penaltyClient.getPenaltiesByDriver(driverId)).thenReturn(payload);

        // Act + Assert
        given()
                .when()
                .get("/api/penalties/drivers/" + driverId)
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].driver_d", equalTo((int) driverId))
                .body("[0].penalty_points", equalTo(2));
    }

    @Test
    @TestSecurity(user = "operator", roles = {"operator"})
    public void test_get_penalties_for_driver_not_found_returns_404() {
        // Arrange
        long missingDriverId = 99999L;
        when(driverService.getDriverById(missingDriverId)).thenReturn(null);

        // Act + Assert
        given()
                .when()
                .get("/api/penalties/drivers/" + missingDriverId)
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "operator", roles = {"operator"})
    public void test_get_penalties_for_trip_as_operator() {
        // Arrange
        long tripId = 77L;
        Trip trip = new Trip();
        trip.setId(tripId);
        when(tripService.getTripById(tripId)).thenReturn(trip);

        TripPenaltiesDto payload = new TripPenaltiesDto(tripId, 1001L, 2002L, 7, Instant.now());
        when(penaltyClient.getPenaltiesByTrip(tripId)).thenReturn(payload);

        // Act + Assert
        given()
                .when()
                .get("/api/penalties/trips/" + tripId)
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].trip_id", equalTo((int) tripId))
                .body("[0].total_penalty_points", equalTo(7));
    }

    @Test
    @TestSecurity(user = "operator", roles = {"operator"})
    public void test_get_penalties_for_trip_not_found_returns_404() {
        // Arrange
        long missingTripId = 123456L;
        when(tripService.getTripById(missingTripId)).thenThrow(new NotFoundException("Trip not found"));

        // Act + Assert
        given()
                .when()
                .get("/api/penalties/trips/" + missingTripId)
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "guest")
    public void test_access_without_role_is_forbidden_or_unauthorized() {
        given()
                .when()
                .get("/api/penalties/")
                .then()
                .statusCode(403);
    }
}
