package com.zimono.trg.a.controller;

import com.zimono.trg.a.client.PenaltyClient;
import com.zimono.trg.a.model.Driver;
import com.zimono.trg.a.model.Trip;
import com.zimono.trg.a.service.DriverService;
import com.zimono.trg.a.service.TripService;
import com.zimono.trg.shared.net.DriverPenaltyDto;
import com.zimono.trg.shared.net.TripPenaltiesDto;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

/**
 * Endpoints proxying to microservice_c
 * for getting penalty evidence.
 *
 */

@Path("/api/penalties")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class PenaltyResource {

    @Inject
    DriverService driverService;
    @Inject
    TripService tripService;

    @Inject
    @RestClient
    PenaltyClient penaltyClient;


    @GET
    @Path("/")
    @RolesAllowed({"admin"})
    @Retry(maxRetries = 3)
    public List<DriverPenaltyDto> getAllPenalties() {
        return penaltyClient.getAllPenalties();
    }

    @GET
    @Path("/drivers/{id: \\d+}")
    @RolesAllowed({"admin", "operator"})
    @Retry(maxRetries = 3)
    public List<DriverPenaltyDto> getPenaltiesForDriver(@PathParam("id") Long id) {
        Driver driver = driverService.getDriverById(id);
        if (driver == null) {
            throw new NotFoundException("Driver not found with id: " + id);
        }
        return penaltyClient.getPenaltiesByDriver(id);
    }

    @GET
    @Path("/trips/{id: \\d+}")
    @RolesAllowed({"admin", "operator"})
    @Retry(maxRetries = 3)
    public TripPenaltiesDto getPenaltiesForTrip(@PathParam("id") Long tripId) {
        Trip trip = tripService.getTripById(tripId);
        if (trip == null) {
            throw new NotFoundException("Trip not found with id: " + tripId);
        }
        return penaltyClient.getPenaltiesByTrip(tripId);
    }
}
