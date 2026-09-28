package com.household.household.service.serviceImpl;

import com.household.household.dto.request.AcResaleCalculationRequest;
import com.household.household.dto.response.AcDepreciationCalculationResponse;
import com.household.household.dto.response.AcProductResponse;
import com.household.household.dto.response.AcResaleCalculationResponse;
import com.household.household.enums.TransactionType;
import com.household.household.service.AcDepreciationRateService;
import com.household.household.service.AcProductSearchService;
import com.household.household.service.AcResaleCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Year;

@Slf4j
@Service
@RequiredArgsConstructor
public class AcResaleCalculationServiceImpl implements AcResaleCalculationService {

    private static final int MONETARY_SCALE = 2;
    private static final int PERCENTAGE_SCALE = 2;
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final AcProductSearchService productSearchService;
    private final AcDepreciationRateService depreciationRateService;
    private final Clock clock;

    @Override
    public AcResaleCalculationResponse calculate(AcResaleCalculationRequest request, TransactionType type) {

        // ── Step 1: Search for the AC product in database ──
        AcProductResponse product = productSearchService.searchProduct(
                request.getBrand(),
                request.getModelName(),
                request.getAcType(),
                request.getCapacityInTon(),
                request.getInverterNonInverter(),
                request.getStarRating()
        );

        // ── Step 2: Use launching price from DATABASE
        BigDecimal launchingPrice = product.getLaunchingPrice();
        int launchingYear = product.getYear();

        // ── Step 3: Get current calendar year dynamically ──
        int currentYear = Year.now(clock).getValue();

        // ── Step 4: Validate year relationship ──
        if (currentYear < launchingYear) {
            throw new IllegalArgumentException(
                    "Current year (" + currentYear + ") cannot be earlier than the AC launching year (" + launchingYear + ").");
        }

        // ── Step 5: Calculate time in years ──
        int timeInYears = currentYear - launchingYear;

        // ── Step 6: Get depreciation rate ──
        BigDecimal depreciationRate = depreciationRateService.getDepreciationRate(type);

        // ── Step 7: Calculate base value using compound depreciation ──
        // Formula: Base Value = Original Cost × (1 − r)^t
        BigDecimal remainingFactor = BigDecimal.ONE.subtract(depreciationRate);
        BigDecimal depreciationFactor = remainingFactor.pow(timeInYears);

        BigDecimal baseValue = launchingPrice
                .multiply(depreciationFactor)
                .setScale(MONETARY_SCALE, RoundingMode.HALF_UP);

        // ── Step 8: Calculate condition multipliers ──
        BigDecimal minMultiplier;
        BigDecimal maxMultiplier;

        switch (request.getCondition()) {
            case GOOD -> {
                minMultiplier = BigDecimal.ONE;
                maxMultiplier = BigDecimal.ONE;
            }
            case FAIR -> {
                minMultiplier = new BigDecimal("0.75");
                maxMultiplier = new BigDecimal("0.85");
            }
            case POOR -> {
                minMultiplier = new BigDecimal("0.40");
                maxMultiplier = new BigDecimal("0.50");
            }
            default -> throw new IllegalArgumentException("Unsupported condition: " + request.getCondition());
        }

        BigDecimal minFinalResaleValue = baseValue.multiply(minMultiplier).setScale(MONETARY_SCALE, RoundingMode.HALF_UP);
        BigDecimal maxFinalResaleValue = baseValue.multiply(maxMultiplier).setScale(MONETARY_SCALE, RoundingMode.HALF_UP);

        // ── Step 9: Calculate remaining value percentage (base) ──
        BigDecimal remainingPercentage = depreciationFactor
                .multiply(HUNDRED)
                .setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP);

        String depreciationRateStr = depreciationRate.multiply(HUNDRED).setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP).toPlainString() + "%";
        String remainingPercentageStr = remainingPercentage.toPlainString() + "%";

        log.info("Depreciation calculation: launchingPrice={}, rate={}, years={}, " +
                        "remainingPct={}, baseValue={}, condition={}, min={}, max={}",
                launchingPrice,
                depreciationRateStr,
                timeInYears,
                remainingPercentageStr,
                baseValue,
                request.getCondition(),
                minFinalResaleValue,
                maxFinalResaleValue);

        // ── Step 10: Build response ──
        AcDepreciationCalculationResponse calculation = AcDepreciationCalculationResponse.builder()
                .launchingYear(launchingYear)
                .currentYear(currentYear)
                .timeInYears(timeInYears)
                .depreciationRate(depreciationRateStr)
                .remainingValuePercentage(remainingPercentageStr)
                .baseValue(baseValue)
                .conditionApplied(request.getCondition().name())
                .minFinalResaleValue(minFinalResaleValue)
                .maxFinalResaleValue(maxFinalResaleValue)
                .build();

        return AcResaleCalculationResponse.builder()
                .product(product)
                .calculation(calculation)
                .build();
    }
}
