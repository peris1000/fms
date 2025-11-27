package com.zimono.trg.shared;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record DriverPenaltyMessage(long driverId, int penaltyPoints) {
}
