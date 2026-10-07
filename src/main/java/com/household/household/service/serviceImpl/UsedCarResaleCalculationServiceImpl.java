package com.household.household.service.serviceImpl;

import com.household.household.dto.request.UsedCarResaleCalculationRequest;
import com.household.household.dto.response.UsedCarDepreciationCalculationResponse;
import com.household.household.dto.response.UsedCarProductResponse;
import com.household.household.dto.response.UsedCarResaleCalculationResponse;
import com.household.household.enums.TransactionType;
import com.household.household.service.UsedCarProductSearchService;
import com.household.household.service.UsedCarResaleCalculationService;
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
public class UsedCarResaleCalculationServiceImpl implements UsedCarResaleCalculationService {

    private static final int MONETARY_SCALE = 2;
    private static final int PERCENTAGE_SCALE = 2;
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    // First year depreciation: 60%
    private static final BigDecimal FIRST_YEAR_DEPRECIATION_RATE = new BigDecimal("0.60");

    // Subsequent years depreciation: 1% of launching price per year
    private static final BigDecimal SUBSEQUENT_YEAR_DEPRECIATION_RATE = new BigDecimal("0.01");

    // KM deduction: 0.05% per every 1,000 km driven
    private static final int KM_BRACKET_SIZE = 1000;
    private static final BigDecimal KM_DEDUCTION_PER_BRACKET = new BigDecimal("0.0005");

    private final UsedCarProductSearchService productSearchService;
    private final Clock clock;

    @Override
    public UsedCarResaleCalculationResponse calculate(UsedCarResaleCalculationRequest request, TransactionType type) {

        // ── Step 1: Search for the Used Car product in database ──
        UsedCarProductResponse product = productSearchService.searchProduct(
                request.getCompany(),
                request.getModelName(),
                request.getVariant(),
                request.getFuelType()
        );

        // ── Step 2: Use launching price from DATABASE ──
        BigDecimal launchingPrice = product.getLaunchingPrice();
        int launchYear = product.getLaunchYear();

        // ── Step 3: Get current calendar year dynamically ──
        int currentYear = Year.now(clock).getValue();

        // ── Step 4: Validate year relationship ──
        if (currentYear < launchYear) {
            throw new IllegalArgumentException(
                    "Current year (" + currentYear + ") cannot be earlier than the vehicle launch year (" + launchYear + ").");
        }

        // ── Step 5: Calculate years of use ──
        int yearsOfUse = currentYear - launchYear;

        // ── Step 6: Calculate base value using depreciation formula ──
        // If yearsOfUse <= 1 (0 to 1 year old): baseValue = launchingPrice × (1 - 0.60)
        // If yearsOfUse > 1:  afterYear1 = launchingPrice × (1 - 0.60)
        //                     baseValue  = afterYear1 - (launchingPrice × 0.01 × (yearsOfUse - 1))
        BigDecimal baseValue;

        // Apply 60% depreciation for 0 to 1 year old (1 to 12 months)
        BigDecimal afterYear1 = launchingPrice
                .multiply(BigDecimal.ONE.subtract(FIRST_YEAR_DEPRECIATION_RATE));

        if (yearsOfUse <= 1) {
            baseValue = afterYear1;
        } else {
            // Apply 1% of launching price deduction for each year after the 1st year
            BigDecimal subsequentDeduction = launchingPrice
                    .multiply(SUBSEQUENT_YEAR_DEPRECIATION_RATE)
                    .multiply(new BigDecimal(yearsOfUse - 1));
            baseValue = afterYear1.subtract(subsequentDeduction).max(BigDecimal.ZERO);
        }

        baseValue = baseValue.setScale(MONETARY_SCALE, RoundingMode.HALF_UP);

        // ── Step 7: Apply condition multipliers ──
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

        BigDecimal minAfterCondition = baseValue.multiply(minMultiplier).setScale(MONETARY_SCALE, RoundingMode.HALF_UP);
        BigDecimal maxAfterCondition = baseValue.multiply(maxMultiplier).setScale(MONETARY_SCALE, RoundingMode.HALF_UP);

        // ── Step 8: Apply KM deduction ──
        // Every 1,000 km → 0.05% deduction on final value
        int kmBrackets = request.getTotalKmDriven() / KM_BRACKET_SIZE;
        BigDecimal kmDeductionRate = KM_DEDUCTION_PER_BRACKET
                .multiply(new BigDecimal(kmBrackets));

        BigDecimal kmRetainFactor = BigDecimal.ONE.subtract(kmDeductionRate);

        BigDecimal minAfterKm = minAfterCondition.multiply(kmRetainFactor).setScale(MONETARY_SCALE, RoundingMode.HALF_UP);
        BigDecimal maxAfterKm = maxAfterCondition.multiply(kmRetainFactor).setScale(MONETARY_SCALE, RoundingMode.HALF_UP);

        // ── Step 9: Apply ownership deduction ──
        // 1st owner → 0%, 2nd owner → 1%, 3rd owner → 2%, 4th owner → 3%, 5th owner → 4%
        int ownerNumber = request.getOwnership().getOwnerNumber();
        BigDecimal ownerDeductionRate = new BigDecimal(ownerNumber - 1)
                .multiply(new BigDecimal("0.01"));

        BigDecimal ownerRetainFactor = BigDecimal.ONE.subtract(ownerDeductionRate);

        BigDecimal minFinalResaleValue = minAfterKm.multiply(ownerRetainFactor).setScale(MONETARY_SCALE, RoundingMode.HALF_UP);
        BigDecimal maxFinalResaleValue = maxAfterKm.multiply(ownerRetainFactor).setScale(MONETARY_SCALE, RoundingMode.HALF_UP);

        // ── Step 10: Prepare percentage strings for response ──
        String firstYearDepreciationStr = FIRST_YEAR_DEPRECIATION_RATE
                .multiply(HUNDRED).setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP).toPlainString() + "%";

        String subsequentYearRateStr = SUBSEQUENT_YEAR_DEPRECIATION_RATE
                .multiply(HUNDRED).setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP).toPlainString() + "%";

        String kmDeductionPercentStr = kmDeductionRate
                .multiply(HUNDRED).setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP).toPlainString() + "%";

        String ownershipDeductionPercentStr = ownerDeductionRate
                .multiply(HUNDRED).setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP).toPlainString() + "%";

        // ── Total base deduction % = ((launchingPrice - baseValue) / launchingPrice) × 100 ──
        BigDecimal totalBaseDeduction = launchingPrice.subtract(baseValue)
                .divide(launchingPrice, 4, RoundingMode.HALF_UP)
                .multiply(HUNDRED)
                .setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP);
        String totalBaseDeductionPercentStr = totalBaseDeduction.toPlainString() + "%";

        // ── Total deduction % including all adjustments (base + condition + km + ownership) ──
        // Uses the average of min and max final resale values for the overall deduction
        BigDecimal avgFinalResale = minFinalResaleValue.add(maxFinalResaleValue)
                .divide(new BigDecimal("2"), MONETARY_SCALE, RoundingMode.HALF_UP);
        BigDecimal totalDeduction = launchingPrice.subtract(avgFinalResale)
                .divide(launchingPrice, 4, RoundingMode.HALF_UP)
                .multiply(HUNDRED)
                .setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP);
        String totalDeductionPercentStr = totalDeduction.toPlainString() + "%";

        log.info("Used Car Depreciation calculation [{}]: launchPrice={}, yearsOfUse={}, " +
                        "baseValue={}, condition={}, minAfterCond={}, maxAfterCond={}, " +
                        "kmDriven={}, kmDeduction={}, owner={}, ownerDeduction={}, " +
                        "minFinal={}, maxFinal={}",
                type,
                launchingPrice,
                yearsOfUse,
                baseValue,
                request.getCondition(),
                minAfterCondition,
                maxAfterCondition,
                request.getTotalKmDriven(),
                kmDeductionPercentStr,
                request.getOwnership(),
                ownershipDeductionPercentStr,
                minFinalResaleValue,
                maxFinalResaleValue);

        // ── Step 11: Build response ──
        UsedCarDepreciationCalculationResponse calculation = UsedCarDepreciationCalculationResponse.builder()
                .launchYear(launchYear)
                .currentYear(currentYear)
                .yearsOfUse(yearsOfUse)
                .firstYearDepreciation(firstYearDepreciationStr)
                .subsequentYearRate(subsequentYearRateStr)
                .totalBaseDeductionPercent(totalBaseDeductionPercentStr)
                .baseValue(baseValue)
                .conditionApplied(request.getCondition().name())
                .minAfterCondition(minAfterCondition)
                .maxAfterCondition(maxAfterCondition)
                .totalKmDriven(request.getTotalKmDriven())
                .totalKmDeductionPercent(kmDeductionPercentStr)
                .ownership(request.getOwnership().name())
                .totalownershipDeductionPercent(ownershipDeductionPercentStr)
                .totalDeductionPercentIncludingBaseAndFinalResale(totalDeductionPercentStr)
                .minFinalResaleValue(minFinalResaleValue)
                .maxFinalResaleValue(maxFinalResaleValue)
                .build();

        return UsedCarResaleCalculationResponse.builder()
                .product(product)
                .calculation(calculation)
                .build();
    }
}