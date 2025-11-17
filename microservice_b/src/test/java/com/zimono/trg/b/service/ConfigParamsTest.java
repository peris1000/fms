package com.zimono.trg.b.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigParamsTest {

    @Test
    void recordAccessors_andEquality() {
        ConfigParams cfg = new ConfigParams(10.5, 99.9, 40.0, -70.0, 0.15);
        assertEquals(10.5, cfg.minSpeed());
        assertEquals(99.9, cfg.maxSpeed());
        assertEquals(40.0, cfg.baseLatitude());
        assertEquals(-70.0, cfg.baseLongitude());
        assertEquals(0.15, cfg.deviation());

        ConfigParams same = new ConfigParams(10.5, 99.9, 40.0, -70.0, 0.15);
        assertEquals(cfg, same);
        assertEquals(cfg.hashCode(), same.hashCode());
        assertTrue(cfg.toString().contains("minSpeed=10.5"));
    }
}
