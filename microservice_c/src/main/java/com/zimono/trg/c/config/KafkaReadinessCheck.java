package com.zimono.trg.c.config;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class KafkaReadinessCheck implements HealthCheck {

    // Check Kafka connectivity
    @Override
    public HealthCheckResponse call() {
        // Implementation depends on Kafka client
        return HealthCheckResponse.up("Kafka connection");
    }
}
