package com.household.household.service;

import com.household.household.enums.AcDepreciationRate;
import com.household.household.enums.AcType;
import com.household.household.service.serviceImpl.AcDepreciationRateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AcDepreciationRateServiceTest {

    private AcDepreciationRateService rateService;

    @BeforeEach
    void setUp() {
        rateService = new AcDepreciationRateServiceImpl();
    }

    @Test
    @DisplayName("Window AC should return 15% depreciation rate")
    void windowAc_shouldReturn15Percent() {
        BigDecimal rate = rateService.getDepreciationRate("Window", "Non-Inverter");
        assertEquals(0, new BigDecimal("0.15").compareTo(rate));
    }

    @Test
    @DisplayName("Split AC should return 15% depreciation rate")
    void splitAc_shouldReturn15Percent() {
        BigDecimal rate = rateService.getDepreciationRate("Split", "Inverter");
        assertEquals(0, new BigDecimal("0.15").compareTo(rate));
    }

    @Test
    @DisplayName("Unknown AC type should throw IllegalArgumentException")
    void unknownAcType_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> rateService.getDepreciationRate("Unknown", "Inverter"));
    }
}
