package com.zimono.trg.c;

import com.zimono.trg.c.utils.PenaltyCalculator;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class PenaltyCalculationServiceTest {

    @Test
    public void testCalculatePenaltyPoints() {
        assertEquals(0, PenaltyCalculator.calculatePenaltyPoints(50));
        assertEquals(10, PenaltyCalculator.calculatePenaltyPoints(65));
        assertEquals(65, PenaltyCalculator.calculatePenaltyPoints(85));
        assertEquals(140, PenaltyCalculator.calculatePenaltyPoints(100));
    }

    @Test
    void testNoSpeedingNoPenalty() {
        assertEquals(0, PenaltyCalculator.calculatePenaltyPoints(50.0));
    }

    @Test
    void testLowSpeedingPenalty() {
        int points = PenaltyCalculator.calculatePenaltyPoints(70.0);
        assertTrue(points > 0 && points < 50);
    }

    @Test
    void testHighSpeedingPenalty() {
        int points = PenaltyCalculator.calculatePenaltyPoints(100.0);
        assertTrue(points > 50);
    }

    @Test
    void testNegativeSpeed() {
        assertEquals(0, PenaltyCalculator.calculatePenaltyPoints(-10.0));
    }
}