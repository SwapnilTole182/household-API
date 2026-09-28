package com.household.household.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcResaleCalculationRequest {


    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Model name is required")
    private String modelName;

    @NotBlank(message = "AC type is required")
    private String acType;

    @NotNull(message = "Capacity in ton is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Capacity must be greater than 0")
    private BigDecimal capacityInTon;

    @NotBlank(message = "Inverter/Non-Inverter type is required")
    private String inverterNonInverter;

    @NotNull(message = "Star rating is required")
    @Positive(message = "Star rating must be a positive number")
    private Integer starRating;

    @NotNull(message = "AC condition is required (GOOD, FAIR, POOR)")
    private com.household.household.enums.ApplianceCondition condition;
}
