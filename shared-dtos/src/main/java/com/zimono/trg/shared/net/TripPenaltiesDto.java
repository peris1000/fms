package com.zimono.trg.shared.net;

import java.time.Instant;

public record TripPenaltiesDto(long trip_id,
                               long car_id,
                               long driver_d,
                               int total_penalty_points,
                               Instant created) {
}
