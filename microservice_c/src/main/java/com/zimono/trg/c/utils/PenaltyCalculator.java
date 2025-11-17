package com.zimono.trg.c.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A static penalty calculator.
 * For speeds above 60 km/h, 2 points are added per km/h.
 * For speeds above 80 km/h, 5 points are added per km/h.
 *
 * @author zimono
 */
public class PenaltyCalculator {
    private static final Logger LOG = LoggerFactory.getLogger(PenaltyCalculator.class);

    private static final double SPEED_THRESHOLD_LOW = 60.0;
    private static final double SPEED_THRESHOLD_HIGH = 80.0;
    private static final int PENALTY_POINTS_LOW = 2;
    private static final int PENALTY_POINTS_HIGH = 5;

    public static int calculatePenaltyPoints(double speed) {
        // validate
        if (speed < 0) {
            LOG.warn("Invalid speed: {} (negative)", speed);
            return 0;
        }

        // calculate
        if (speed > SPEED_THRESHOLD_HIGH) { // > 80 km/h
            double pen = ((SPEED_THRESHOLD_HIGH - SPEED_THRESHOLD_LOW) * PENALTY_POINTS_LOW) +
                    ((speed - SPEED_THRESHOLD_HIGH) * PENALTY_POINTS_HIGH);
            return (int) Math.round(pen);
        } else if (speed > SPEED_THRESHOLD_LOW) { // > 60 km/h
            double pen = ((speed - SPEED_THRESHOLD_LOW) * PENALTY_POINTS_LOW);
            return (int) Math.round(pen);
        }
        return 0;
    }
}
