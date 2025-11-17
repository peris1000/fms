package com.zimono.trg.a.config;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class KafkaReadinessCheckTest {

    @Inject
    @Readiness
    KafkaReadinessCheck kafkaReadinessCheck;

    @Test
    public void testCall_AlwaysReturnsUp() {
        // When
        HealthCheckResponse response = kafkaReadinessCheck.call();

        // Then
        assertNotNull(response);
        assertEquals(HealthCheckResponse.Status.UP, response.getStatus());
        assertEquals("Kafka connection", response.getName());
    }

    @Test
    public void testCall_MultipleInvocations() {
        // When - call multiple times
        HealthCheckResponse response1 = kafkaReadinessCheck.call();
        HealthCheckResponse response2 = kafkaReadinessCheck.call();
        HealthCheckResponse response3 = kafkaReadinessCheck.call();

        // Then - all should be UP
        assertEquals(HealthCheckResponse.Status.UP, response1.getStatus());
        assertEquals(HealthCheckResponse.Status.UP, response2.getStatus());
        assertEquals(HealthCheckResponse.Status.UP, response3.getStatus());
    }
}
