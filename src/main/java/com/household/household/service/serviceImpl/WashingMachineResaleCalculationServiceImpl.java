package com.household.household.service.serviceImpl;

import com.household.household.dto.request.WashingMachineResaleCalculationRequest;
import com.household.household.dto.response.WashingMachineDepreciationCalculationResponse;
import com.household.household.dto.response.WashingMachineProductResponse;
import com.household.household.dto.response.WashingMachineResaleCalculationResponse;
import com.household.household.enums.TransactionType;
import com.household.household.enums.WashingType;
import com.household.household.service.WashingMachineProductSearchService;
import com.household.household.service.WashingMachineResaleCalculationService;
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
public class WashingMachineResaleCalculationServiceImpl implements WashingMachineResaleCalculationService {

    private static final int MONETARY_SCALE = 2;
    private static final int PERCENTAGE_SCALE = 2;
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    /**
     * Customer to Customer depreciation rates by washing type:
     * FULLY_AUTOMATIC: 18% per year
     * SEMI_AUTOMATIC: 15% per year
     * WASHER_DRYER: 15% per year
     */
    private static final BigDecimal C2C_FULLY_AUTOMATIC_RATE = new BigDecimal("0.18");
    private static final BigDecimal C2C_SEMI_AUTOMATIC_RATE = new BigDecimal("0.15");
    private static final BigDecimal C2C_WASHER_DRYER_RATE = new BigDecimal("0.15");

    /**
     * Dealer to Customer (resale) depreciation rates by washing type:
     * FULLY_AUTOMATIC: 13% per year
     * SEMI_AUTOMATIC: 10% per year
     * WASHER_DRYER: 10% per year
     */
    private static final BigDecimal D2C_FULLY_AUTOMATIC_RATE = new BigDecimal("0.13");
    private static final BigDecimal D2C_SEMI_AUTOMATIC_RATE = new BigDecimal("0.10");
    private static final BigDecimal D2C_WASHER_DRYER_RATE = new BigDecimal("0.10");

    private final WashingMachineProductSearchService productSearchService;
    private final Clock clock;

    @Override
    public WashingMachineResaleCalculationResponse calculate(WashingMachineResaleCalculationRequest request, TransactionType type) {

        // ── Step 1: Search for the Washing Machine product in database ──
        WashingMachineProductResponse product = productSearchService.searchProduct(
                request.getBrand(),
                request.getModelNumber(),
                request.getWashingType(),
                request.getCapacityKg(),
                request.getLoadingType()
        );

        // ── Step 2: Use launching price from DATABASE ──
        BigDecimal launchingPrice = product.getLaunchingPrice();
        int launchingYear = product.getYear();

        // ── Step 3: Get current calendar year dynamically ──
        int currentYear = Year.now(clock).getValue();

        // ── Step 4: Validate year relationship ──
        if (currentYear < launchingYear) {
            throw new IllegalArgumentException(
                    "Current year (" + currentYear + ") cannot be earlier than the Washing Machine launching year (" + launchingYear + ").");
        }

        // ── Step 5: Calculate time in years ──
        int timeInYears = currentYear - launchingYear;

        // ── Step 6: Get depreciation rate based on washing type and transaction type ──
        WashingType washingType = WashingType.fromString(request.getWashingType());
        BigDecimal depreciationRate = getDepreciationRate(washingType, type);

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

        log.info("WM Depreciation calculation [{}]: launchingPrice={}, rate={}, years={}, " +
                        "remainingPct={}, baseValue={}, condition={}, min={}, max={}",
                type,
                launchingPrice,
                depreciationRateStr,
                timeInYears,
                remainingPercentageStr,
                baseValue,
                request.getCondition(),
                minFinalResaleValue,
                maxFinalResaleValue);

        // ── Step 10: Build response ──
        WashingMachineDepreciationCalculationResponse calculation = WashingMachineDepreciationCalculationResponse.builder()
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

        return WashingMachineResaleCalculationResponse.builder()
                .product(product)
                .calculation(calculation)
                .build();
    }

    /**
     * Returns the depreciation rate based on washing type and transaction type.
     *
     * Customer to Customer: higher rates (18%, 15%, 15%)
     * Dealer to Customer: lower rates (13%, 10%, 10%)
     *
     * @param washingType the type of washing machine
     * @param transactionType the transaction type
     * @return the depreciation rate as a decimal (e.g., 0.18 for 18%)
     */
    private BigDecimal getDepreciationRate(WashingType washingType, TransactionType transactionType) {
        if (transactionType == TransactionType.DEALER_TO_CUSTOMER) {
            return switch (washingType) {
                case FULLY_AUTOMATIC -> D2C_FULLY_AUTOMATIC_RATE;
                case SEMI_AUTOMATIC -> D2C_SEMI_AUTOMATIC_RATE;
                case WASHER_DRYER -> D2C_WASHER_DRYER_RATE;
            };
        }
        // Default: CUSTOMER_TO_CUSTOMER
        return switch (washingType) {
            case FULLY_AUTOMATIC -> C2C_FULLY_AUTOMATIC_RATE;
            case SEMI_AUTOMATIC -> C2C_SEMI_AUTOMATIC_RATE;
            case WASHER_DRYER -> C2C_WASHER_DRYER_RATE;
        };
    }
}
