package com.zimono.trg.shared;

import java.time.Instant;

public record Heartbeat(Long tripId,
                        Long carId,
                        Long driverId,
                        Double latitude,
                        Double longitude,
                        Double speed,
                        Instant timestamp) {

}
