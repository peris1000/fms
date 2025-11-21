package com.zimono.trg.a.client;

import com.zimono.trg.shared.net.DriverPenaltyDto;
import com.zimono.trg.shared.net.TripPenaltiesDto;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@RegisterRestClient(configKey = "penalty-api")
@Path("/api/penalties")
@Produces(MediaType.APPLICATION_JSON)
public interface PenaltyClient {

    @GET
    List<DriverPenaltyDto> getAllPenalties();

    @GET
    @Path("/drivers/{driverId}")
    List<DriverPenaltyDto> getPenaltiesByDriver(@PathParam("driverId") Long driverId);

    @GET
    @Path("/trips/{tripId}")
    TripPenaltiesDto getPenaltiesByTrip(@PathParam("tripId") Long tripId);
}
