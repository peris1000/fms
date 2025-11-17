package com.zimono.trg.shared.net;

import java.time.Instant;

public record DriverPenaltyDto(long driver_penalty_id,
                               long trip_id,
                               long car_id,
                               long driver_d,
                               int penalty_points,
                               Instant created) {
}
