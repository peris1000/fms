package com.zimono.trg.a.controller;

import com.zimono.trg.a.dto.*;
import com.zimono.trg.a.model.Trip;
import com.zimono.trg.a.repository.TripRepository;
import com.zimono.trg.a.service.TripService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/trips")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TripResource {
    private static final Logger LOG = LoggerFactory.getLogger(TripResource.class);

    @Inject
    TripService tripService;
    @Inject
    TripRepository repo;

    @GET
    public List<TripDto> getAllTrips() {
        LOG.info("Getting all trips");
        return tripService.getAllTrips().stream()
                .map(TripDto::fromEntity)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{id}")
    public TripDto getTrip(@PathParam("id") Long id) {
        Trip entity = tripService.getTripById(id);
        return TripDto.fromEntity(entity);
    }

    @POST
    public Response createTrip(@Valid TripDto dto) {
        Trip entity = tripService.createTrip(dto);
        return Response.status(Response.Status.CREATED).entity(TripDto.fromEntity(entity)).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateTrip(@PathParam("id") Long id, @Valid TripDto dto) {
        Trip entity = tripService.update(id, dto);
        return Response.status(Response.Status.OK).entity(TripDto.fromEntity(entity)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteTrip(@PathParam("id") Long id) {
        tripService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/start")
    public Response startTrip(@PathParam("id") Long id) {
        Trip entity = tripService.startTrip(id);
        return Response.status(Response.Status.OK).entity(TripDto.fromEntity(entity)).build();
    }

    @POST
    @Path("/{id}/stop")
    public Response stopTrip(@PathParam("id") Long id) {
        Trip entity = tripService.stopTrip(id);
        return Response.status(Response.Status.OK).entity(TripDto.fromEntity(entity)).build();
    }
}