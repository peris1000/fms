package com.zimono.trg.c.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.util.Properties;


@ApplicationScoped
@Readiness
public class KafkaReadinessCheck implements HealthCheck {

    private final AdminClient adminClient;

    @Inject
    public KafkaReadinessCheck(@ConfigProperty(name = "kafka.bootstrap.servers") String bootstrapServers) {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("request.timeout.ms", "3000");
        props.put("default.api.timeout.ms", "3000");
        this.adminClient = AdminClient.create(props);
    }

    @Override
    public HealthCheckResponse call() {
        try {
            // a lightweight call – no topic fetch
            adminClient
                    .listTopics(new ListTopicsOptions().timeoutMs(2000))
                    .listings()
                    .get();
            return HealthCheckResponse.up("Broker connection");
        } catch (Exception e) {
            return HealthCheckResponse.down("Broker connection");
        }
    }
}