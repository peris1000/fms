package com.zimono.trg.b.kafka;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@Testcontainers
public class KafkaServiceTest {

    @Container
    private static final ConfluentKafkaContainer container =
            new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.5"));

    @Test
    public void testKafkaContainer() {
        assertTrue(container.isRunning());
    }
}
