package com.household.household.dto.request;

import com.household.household.enums.ApplianceCondition;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WashingMachineResaleCalculationRequest {

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Model number is required")
    private String modelNumber;

    @NotBlank(message = "Washing type is required")
    private String washingType;

    @NotNull(message = "Capacity in Kg is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Capacity must be greater than 0")
    private BigDecimal capacityKg;

    @NotBlank(message = "Loading type is required")
    private String loadingType;

    @NotNull(message = "Condition is required (GOOD, FAIR, POOR)")
    private ApplianceCondition condition;
}
