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
public class AcDepreciationCalculationResponse {

    private Integer launchingYear;
    private Integer currentYear;
    private Integer timeInYears;
    private String depreciationRate;
    private String remainingValuePercentage;
    
    private BigDecimal baseValue;
    private String conditionApplied;
    private BigDecimal minFinalResaleValue;
    private BigDecimal maxFinalResaleValue;
}
