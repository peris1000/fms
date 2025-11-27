package com.zimono.trg.shared;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.Instant;

@RegisterForReflection
public record Heartbeat(Long tripId,
                        Long carId,
                        Long driverId,
                        Double latitude,
                        Double longitude,
                        Double speed,
                        Instant timestamp) {

}
