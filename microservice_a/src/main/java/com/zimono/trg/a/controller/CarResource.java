package com.zimono.trg.a.controller;

import com.zimono.trg.a.dto.CarAssignmentRequest;
import com.zimono.trg.a.dto.CarDto;
import com.zimono.trg.a.dto.CarSearchRequest;
import com.zimono.trg.a.dto.PaginatedResponse;
import com.zimono.trg.a.model.Car;
import com.zimono.trg.a.repository.CarRepository;
import com.zimono.trg.a.service.CarService;
import io.quarkus.panache.common.Sort;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/cars")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CarResource {
    private static final Logger LOG = LoggerFactory.getLogger(CarResource.class);

    @Inject
    CarService carService;
    @Inject
    CarRepository repo;

    @POST
    @Path("/search")
    public Response searchCars(
            @Nullable CarSearchRequest request,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("sort") @DefaultValue("id") String sort,
            @QueryParam("order") @DefaultValue("asc") String order
    ) {
//        PanacheQuery<Car> fordCars = Car.find("brand", request.brand);
        Sort sortConfig = order.equalsIgnoreCase("desc")
                ? Sort.by(sort).descending()
                : Sort.by(sort).ascending();
        if (size > 100) {
            size = 100;
        }
        if (page < 0) {
            page = 0;
        }
        PaginatedResponse<Car> response = carService.search(request, page, size, sortConfig);
        return Response.ok(response).build();
    }

    @GET
    public List<CarDto> getAllCars() {
        LOG.info("Getting all cars");
        return carService.getAllCars().stream()
                .map(CarDto::fromEntity)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/{id}")
    public CarDto getCar(@PathParam("id") Long id) {
        Car entity = carService.getCarById(id);
        return CarDto.fromEntity(entity);
    }

    @POST
    public Response createCar(@Valid CarDto dto) {
        Car entity = carService.createCar(dto);
        return Response.status(Response.Status.CREATED).entity(CarDto.fromEntity(entity)).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateCar(@PathParam("id") Long id, @Valid CarDto dto) {
        Car entity = carService.update(id, dto);
        return Response.status(Response.Status.OK).entity(CarDto.fromEntity(entity)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteCar(@PathParam("id") Long id) {
        carService.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/assign-driver")
    public Response assignDriver(@Valid CarAssignmentRequest request) {
        Car car = carService.assignDriverToCar(request);
        return Response.status(Response.Status.OK).entity(CarDto.fromEntity(car)).build();
    }

    @POST
    @Path("/{carId}/unassign-driver")
    public Response unassignDriver(@PathParam("carId") Long carId) {
        Car car = carService.unassignDriverFromCar(carId);
        return Response.status(Response.Status.OK).entity(CarDto.fromEntity(car)).build();
    }
}