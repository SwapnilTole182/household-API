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
    private String totalBaseDeductionPercent;

    private BigDecimal baseValue;

    private String conditionApplied;
    private BigDecimal minAfterCondition;
    private BigDecimal maxAfterCondition;

    private Integer totalKmDriven;
    private String totalKmDeductionPercent;

    private String ownership;
    private String totalownershipDeductionPercent;

    private String totalDeductionPercentIncludingBaseAndFinalResale;

    private BigDecimal minFinalResaleValue;
    private BigDecimal maxFinalResaleValue;
}
