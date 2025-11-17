package com.zimono.trg.a.controller;

import com.zimono.trg.a.client.PenaltyClient;
import com.zimono.trg.a.dto.DriverDto;
import com.zimono.trg.a.service.DriverService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/drivers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DriverResource {

    @Inject
    DriverService driverService;

    @GET
    public List<DriverDto> getAllDrivers() {
        return driverService.getAllDrivers().stream()
                .map(DriverDto::fromEntity)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{id: \\d+}")
    public DriverDto getDriver(@PathParam("id") Long id) {
        return DriverDto.fromEntity(driverService.getDriverById(id));
    }

    @POST
    public Response createDriver(@Valid DriverDto driverDto) {
        DriverDto createdDriver = DriverDto.fromEntity(driverService.createDriver(driverDto));
        return Response.status(Response.Status.CREATED).entity(createdDriver).build();
    }

    @PUT
    @Path("/{id: \\d+}")
    public DriverDto updateDriver(@PathParam("id") Long id, @Valid DriverDto driverDto) {
        return DriverDto.fromEntity(driverService.updateDriver(id, driverDto));
    }

    @DELETE
    @Path("/{id: \\d+}")
    public Response deleteDriver(@PathParam("id") Long id) {
        driverService.deleteDriver(id);
        return Response.noContent().build();
    }

}
