package com.zimono.trg.c.controller;

import com.zimono.trg.c.model.DriverPenalty;
import com.zimono.trg.c.repository.DriverPenaltyRepository;
import com.zimono.trg.shared.net.DriverPenaltyDto;
import com.zimono.trg.shared.net.TripPenaltiesDto;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.stream.Collectors;


@Path("/api/penalties")
@Produces(MediaType.APPLICATION_JSON)
public class PenaltyResource {

    @Inject
    DriverPenaltyRepository repository;

    @GET
    @Path("/drivers/{driverId}")
    @Blocking //TODO:
    public List<DriverPenaltyDto> getPenaltyPoints(@PathParam("driverId") Long driverId) {
        List<DriverPenalty> penalties = repository
                .getPenaltiesByDriverId(driverId).await().indefinitely();
        return penalties.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/trips/{tripId}")
    public TripPenaltiesDto getTripPenaltyPoints(@PathParam("tripId") Long tripId) {
        Uni<TripPenaltiesDto> tripPenalties = repository.findByTripId(tripId);
        return tripPenalties.await().indefinitely();
    }

    @GET
    public List<DriverPenaltyDto> getAllPenalties() {
        List<DriverPenalty> penalties = repository.listAll();
        return penalties.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private DriverPenaltyDto toDto(DriverPenalty entity) {
        return new DriverPenaltyDto(
                entity.getId(),
                entity.getTripId(),
                entity.getCarId(),
                entity.getDriverId(),
                entity.getPenaltyPoints(),
                entity.getCreatedAt()
        );
    }
}
