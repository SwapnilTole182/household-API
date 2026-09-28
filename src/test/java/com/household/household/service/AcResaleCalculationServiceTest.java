package com.household.household.service;

import com.household.household.dto.request.AcResaleCalculationRequest;
import com.household.household.dto.response.AcProductResponse;
import com.household.household.dto.response.AcResaleCalculationResponse;
import com.household.household.enums.AcCondition;
import com.household.household.exception.AcProductNotFoundException;
import com.household.household.service.serviceImpl.AcResaleCalculationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcResaleCalculationServiceTest {

    @Mock
    private AcProductSearchService productSearchService;

    @Mock
    private AcDepreciationRateService depreciationRateService;

    private AcResaleCalculationServiceImpl calculationService;

    // Fixed to year 2026 for all tests
    private static final int CURRENT_YEAR = 2026;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(
                LocalDate.of(CURRENT_YEAR, 6, 15)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant(),
                ZoneId.systemDefault()
        );
        calculationService = new AcResaleCalculationServiceImpl(
                productSearchService, depreciationRateService, fixedClock
        );
    }

    @Test
    @DisplayName("Window AC with GOOD condition should calculate min/max as 100% of base")
    void windowAc_GoodCondition() {
        AcResaleCalculationRequest request = buildRequest("Daikin", "Model X", "Window", "Non-Inverter", AcCondition.GOOD);
        mockProductSearch(request, 2022, new BigDecimal("50000"));
        when(depreciationRateService.getDepreciationRate("Window", "Non-Inverter"))
                .thenReturn(new BigDecimal("0.15"));

        AcResaleCalculationResponse response = calculationService.calculate(request);

        assertEquals(0, new BigDecimal("26100.31").compareTo(response.getCalculation().getBaseValue()));
        assertEquals(0, new BigDecimal("26100.31").compareTo(response.getCalculation().getMinFinalResaleValue()));
        assertEquals(0, new BigDecimal("26100.31").compareTo(response.getCalculation().getMaxFinalResaleValue()));
        assertEquals("GOOD", response.getCalculation().getConditionApplied());
        assertEquals(4, response.getCalculation().getTimeInYears());
    }

    @Test
    @DisplayName("Split AC with FAIR condition should calculate min as 75% and max as 85%")
    void splitAc_FairCondition() {
        AcResaleCalculationRequest request = buildRequest("LG", "LG Split", "Split", "Inverter", AcCondition.FAIR);
        mockProductSearch(request, 2022, new BigDecimal("50000"));
        when(depreciationRateService.getDepreciationRate("Split", "Inverter"))
                .thenReturn(new BigDecimal("0.15"));

        AcResaleCalculationResponse response = calculationService.calculate(request);

        assertEquals(0, new BigDecimal("26100.31").compareTo(response.getCalculation().getBaseValue()));
        assertEquals(0, new BigDecimal("19575.23").compareTo(response.getCalculation().getMinFinalResaleValue()));
        assertEquals(0, new BigDecimal("22185.26").compareTo(response.getCalculation().getMaxFinalResaleValue()));
        assertEquals("FAIR", response.getCalculation().getConditionApplied());
    }

    @Test
    @DisplayName("Split AC with POOR condition should calculate min as 40% and max as 50%")
    void inverterAc_PoorCondition() {
        AcResaleCalculationRequest request = buildRequest("Samsung", "Samsung Inv", "Split", "Inverter", AcCondition.POOR);
        mockProductSearch(request, 2022, new BigDecimal("50000"));
        when(depreciationRateService.getDepreciationRate("Split", "Inverter"))
                .thenReturn(new BigDecimal("0.15"));

        AcResaleCalculationResponse response = calculationService.calculate(request);

        assertEquals(0, new BigDecimal("26100.31").compareTo(response.getCalculation().getBaseValue()));
        assertEquals(0, new BigDecimal("10440.12").compareTo(response.getCalculation().getMinFinalResaleValue()));
        assertEquals(0, new BigDecimal("13050.16").compareTo(response.getCalculation().getMaxFinalResaleValue()));
        assertEquals("POOR", response.getCalculation().getConditionApplied());
    }

    @Test
    @DisplayName("t=0 should return resale value equal to launching price")
    void timeZero_resaleEqualsLaunchPrice() {
        AcResaleCalculationRequest request = buildRequest("LG", "LG New", "Split", "Inverter", AcCondition.GOOD);
        mockProductSearch(request, CURRENT_YEAR, new BigDecimal("50000"));
        when(depreciationRateService.getDepreciationRate("Split", "Inverter"))
                .thenReturn(new BigDecimal("0.15"));

        AcResaleCalculationResponse response = calculationService.calculate(request);

        assertEquals(0, new BigDecimal("50000.00").compareTo(response.getCalculation().getBaseValue()));
        assertEquals(0, new BigDecimal("50000.00").compareTo(response.getCalculation().getMinFinalResaleValue()));
        assertEquals(0, response.getCalculation().getTimeInYears());
    }

    @Test
    @DisplayName("Current year before launching year should throw IllegalArgumentException")
    void currentYearBeforeLaunchingYear_shouldThrow() {
        AcResaleCalculationRequest request = buildRequest("LG", "Future AC", "Split", "Inverter", AcCondition.GOOD);
        mockProductSearch(request, 2030, new BigDecimal("50000"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> calculationService.calculate(request));

        assertTrue(ex.getMessage().contains("cannot be earlier"));
    }

    @Test
    @DisplayName("Product not found should throw AcProductNotFoundException")
    void productNotFound_shouldThrow() {
        AcResaleCalculationRequest request = buildRequest("Unknown", "Fake AC", "Split", "Inverter", AcCondition.GOOD);
        when(productSearchService.searchProduct(
                eq(request.getBrand()), eq(request.getModelName()),
                eq(request.getAcType()), eq(request.getCapacityInTon()),
                eq(request.getInverterNonInverter()), eq(request.getStarRating())
        )).thenThrow(new AcProductNotFoundException());

        assertThrows(AcProductNotFoundException.class,
                () -> calculationService.calculate(request));
    }

    @Test
    @DisplayName("Response should contain correct product and calculation fields")
    void responseStructure_shouldBeComplete() {
        AcResaleCalculationRequest request = buildRequest("LG", "LG 1.5 Ton", "Split", "Inverter", AcCondition.GOOD);
        mockProductSearch(request, 2022, new BigDecimal("52000"));
        when(depreciationRateService.getDepreciationRate("Split", "Inverter"))
                .thenReturn(new BigDecimal("0.15"));

        AcResaleCalculationResponse response = calculationService.calculate(request);

        // Verify product section
        assertNotNull(response.getProduct());
        assertEquals(2022, response.getProduct().getYear());
        assertEquals("LG", response.getProduct().getBrand());
        assertEquals(0, new BigDecimal("52000").compareTo(response.getProduct().getLaunchingPrice()));

        // Verify calculation section
        assertNotNull(response.getCalculation());
        assertEquals(2022, response.getCalculation().getLaunchingYear());
        assertEquals(CURRENT_YEAR, response.getCalculation().getCurrentYear());
        assertEquals(4, response.getCalculation().getTimeInYears());
        assertEquals("15.00%", response.getCalculation().getDepreciationRate());

        // 52000 × 0.85^4 = 27144.33
        assertEquals(0, new BigDecimal("27144.33").compareTo(response.getCalculation().getBaseValue()));
        assertEquals(0, new BigDecimal("27144.33").compareTo(response.getCalculation().getMinFinalResaleValue()));
        assertEquals("GOOD", response.getCalculation().getConditionApplied());
    }

    // ═══════════════════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════════════════

    private AcResaleCalculationRequest buildRequest(String brand, String modelName,
                                                     String acType, String inverterNonInverter, AcCondition condition) {
        return AcResaleCalculationRequest.builder()
                .brand(brand)
                .modelName(modelName)
                .acType(acType)
                .capacityInTon(new BigDecimal("1.5"))
                .inverterNonInverter(inverterNonInverter)
                .starRating(5)
                .condition(condition)
                .build();
    }

    private void mockProductSearch(AcResaleCalculationRequest req, int launchingYear, BigDecimal launchingPrice) {
        AcProductResponse product = AcProductResponse.builder()
                .year(launchingYear)
                .brand(req.getBrand())
                .modelName(req.getModelName())
                .acType(req.getAcType())
                .capacityInTon(req.getCapacityInTon())
                .inverterNonInverter(req.getInverterNonInverter())
                .starRating(req.getStarRating())
                .launchingPrice(launchingPrice)
                .build();

        when(productSearchService.searchProduct(
                eq(req.getBrand()), eq(req.getModelName()),
                eq(req.getAcType()), eq(req.getCapacityInTon()),
                eq(req.getInverterNonInverter()), eq(req.getStarRating())
        )).thenReturn(product);
    }
}
