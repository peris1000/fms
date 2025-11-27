package com.zimono.trg.b;

import com.zimono.trg.b.kafka.KafkaTestResource;
import com.zimono.trg.b.service.*;
import com.zimono.trg.shared.TripMessage;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(KafkaTestResource.class)
public class CarSimulatorServiceTest {

    @Inject
    @ConfigProperty(name = "kafka.bootstrap.servers")
    String bootstrapServers;

    @Inject
    CarSimulatorService carSimulator;
    private static final int MESSAGE_COUNT = 10000;
    private static final String TOPIC = "car-heartbeats";
    private static final String GROUP = "test-group";

    @Test
    public void test_HeartbeatGeneration_does_not_throw_exception() {
        assertDoesNotThrow(() -> {
            carSimulator.generateScheduledHeartbeat();
        });
    }

    @Test
    public void test_HeartbeatGeneration_stress_test() throws InterruptedException {

        long productionStartTime = System.currentTimeMillis();

        for (int i = 1; i <= MESSAGE_COUNT; i++) {
            long tripId = i;
            long carId = i;
            long driverId = i;
            carSimulator.addTrip(new TripMessage(tripId, carId, driverId));
        }
        carSimulator.generateScheduledHeartbeat();
        // Record time after production completes
        long productionEndTime = System.currentTimeMillis();
        long productionDuration = productionEndTime - productionStartTime;
        System.out.println("Production took: " + productionDuration + "ms");


//        // Consume message
//        try (KafkaConsumer<String, String> consumer = createConsumer(bootstrapServers, GROUP)) {
//            consumer.subscribe(Collections.singletonList(TOPIC));
//            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(20));
//
//            assertFalse(records.isEmpty(), "No records consumed");
//
////            String expectedText = "{\"tripId\":" + last + ",\"carId\":" + last + ",\"driverId\":" + last + ",";
//            String firstRecordValue = records.iterator().next().value();
//            String expectedText = "{\"tripId\":1,\"carId\":1,\"driverId\":1,";
//            assertTrue(firstRecordValue.startsWith(expectedText),
//                    String.format("Record value '%s' should start '%s'", firstRecordValue, expectedText));
//        }

        ConsumeResult consumeResult = consumeMessagesUntilLast();

        // Calculate total time from first production to last consumption
        long totalTime = consumeResult.lastRecordConsumptionTime - productionStartTime;
        long consumptionDuration = consumeResult.lastRecordConsumptionTime - productionEndTime;

        System.out.println("Total time (production to last consumption): " + totalTime + "ms");
        System.out.println("Consumption time (after production to last consumption): " + consumptionDuration + "ms");
        System.out.println("Messages consumed: " + consumeResult.messageCount);

        // Verify the last record
        String lastRecordValue = consumeResult.lastRecordValue;
        String expectedLastMessage = "{\"tripId\":" + MESSAGE_COUNT + ",\"carId\":" + MESSAGE_COUNT + ",\"driverId\":" + MESSAGE_COUNT + ",";
        assertTrue(lastRecordValue.startsWith(expectedLastMessage),
                "Last record should be the last message sent");

        // Optional: Assert on timing (adjust thresholds based on your needs)
        assertTrue(totalTime < 10000, "Total time should be less than 10 seconds");
        assertTrue(consumeResult.messageCount >= MESSAGE_COUNT,
                "Should consume at least the number of messages sent");
    }

    private ConsumeResult consumeMessagesUntilLast() {
        try (KafkaConsumer<String, String> consumer = createConsumer(bootstrapServers,GROUP)) {
            consumer.subscribe(Collections.singletonList(TOPIC));

            String lastRecordValue = null;
            long lastRecordConsumptionTime = 0;
            int messageCount = 0;
            int consecutiveEmptyPolls = 0;
            final int MAX_CONSECUTIVE_EMPTY_POLLS = 5;

            // Consume for up to 30 seconds or until we stop getting messages
            long startConsumptionTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startConsumptionTime < 30000 &&
                    consecutiveEmptyPolls < MAX_CONSECUTIVE_EMPTY_POLLS) {

                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(2));

                if (records.isEmpty()) {
                    consecutiveEmptyPolls++;
                    continue;
                }

                consecutiveEmptyPolls = 0; // Reset counter since we got messages

                for (var record : records) {
                    lastRecordValue = record.value();
                    lastRecordConsumptionTime = System.currentTimeMillis();
                    messageCount++;

                    System.out.println("Consumed: " + lastRecordValue + " at " + lastRecordConsumptionTime);
                }
            }

            if (lastRecordValue == null) {
                throw new RuntimeException("No messages consumed within timeout");
            }

            return new ConsumeResult(lastRecordValue, lastRecordConsumptionTime, messageCount);
        }
    }

    // Helper class to return multiple values from consumption
    private static class ConsumeResult {
        final String lastRecordValue;
        final long lastRecordConsumptionTime;
        final int messageCount;

        ConsumeResult(String lastRecordValue, long lastRecordConsumptionTime, int messageCount) {
            this.lastRecordValue = lastRecordValue;
            this.lastRecordConsumptionTime = lastRecordConsumptionTime;
            this.messageCount = messageCount;
        }
    }

    private KafkaConsumer<String, String> createConsumer(String bootstrapServers, String groupId) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        return new KafkaConsumer<>(props);
    }
}
