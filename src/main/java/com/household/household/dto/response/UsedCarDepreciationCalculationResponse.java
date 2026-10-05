package com.household.household.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsedCarDepreciationCalculationResponse {

    private Integer launchYear;
    private Integer currentYear;
    private Integer yearsOfUse;

    private String firstYearDepreciation;
    private String subsequentYearRate;

    private BigDecimal baseValue;

    private String conditionApplied;
    private BigDecimal minAfterCondition;
    private BigDecimal maxAfterCondition;

    private Integer totalKmDriven;
    private String kmDeductionPercent;

    private String ownership;
    private String ownershipDeductionPercent;

    private BigDecimal minFinalResaleValue;
    private BigDecimal maxFinalResaleValue;
}
