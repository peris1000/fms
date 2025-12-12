package com.zimono.trg.b.kafka;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.kafka.KafkaContainer;

import java.util.Map;

public class KafkaTestResource implements QuarkusTestResourceLifecycleManager {

    private KafkaContainer container;

    @Override
    public Map<String, String> start() {
        container = new KafkaContainer("apache/kafka:4.1.1");
        container.start();
        return Map.of("kafka.bootstrap.servers", container.getBootstrapServers());
    }

    @Override
    public void stop() {
        if (container != null) {
            container.stop();
        }
    }

//    private RedpandaContainer container;
//
//    @Override
//    public Map<String, String> start() {
//        container = new RedpandaContainer("redpandadata/redpanda:v24.1.2");
//        container.start();
//        return Map.of(
//                "kafka.bootstrap.servers", container.getBootstrapServers()
//        );
//    }
//
//    @Override
//    public void stop() {
//        if (container != null) {
//            container.stop();
//        }
//    }

//    private ConfluentKafkaContainer container;
//
//    @Override
//    public Map<String, String> start() {
//        container = new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.5"));
//        container.start();
//
//        Map<String, String> properties = new HashMap<>();
//        properties.put("kafka.bootstrap.servers", container.getBootstrapServers());
//        properties.put("mp.messaging.connector.smallrye-kafka.bootstrap.servers", container.getBootstrapServers());
//        return properties;
//    }
//
//    @Override
//    public void stop() {
//        if (container != null) {
//            container.stop();
//        }
//    }
//
//    // Helper method to get the bootstrap servers in tests
//    public String getBootstrapServers() {
//        return container.getBootstrapServers();
//    }


}

