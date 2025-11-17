package com.zimono.trg.c.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import javax.sql.DataSource;

@Liveness
@ApplicationScoped
public class DatabaseLivenessCheck implements HealthCheck {

    @Inject
    DataSource dataSource;

    @Override
    public HealthCheckResponse call() {
        try {
            dataSource.getConnection().close();
            return HealthCheckResponse.up("Database connection");
        } catch (Exception e) {
            return HealthCheckResponse.down("Database connection");
        }
    }
}