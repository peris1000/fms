package com.zimono.trg.b;

import com.zimono.trg.b.service.CarSimulatorService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class CarSimulatorServiceTest {

    @Inject
    CarSimulatorService carSimulator;

    @Test
    public void test_HeartbeatGeneration_does_not_throw_exception() {
        assertDoesNotThrow(() -> {
            carSimulator.generateScheduledHeartbeat();
        });
    }
}
