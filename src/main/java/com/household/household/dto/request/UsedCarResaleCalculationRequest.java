package com.household.household.dto.request;

import com.household.household.enums.ApplianceCondition;
import com.household.household.enums.VehicleOwnership;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsedCarResaleCalculationRequest {

    @NotBlank(message = "Company is required")
    private String company;

    @NotBlank(message = "Model name is required")
    private String modelName;

    @NotBlank(message = "Variant is required")
    private String variant; 

    @NotBlank(message = "Fuel type is required")
    private String fuelType;

    @NotNull(message = "Condition is required (GOOD, FAIR, POOR)")
    private ApplianceCondition condition;

    @NotNull(message = "Total KM driven is required")
    @Min(value = 0, message = "Total KM driven must be 0 or greater")
    private Integer totalKmDriven;

    @NotNull(message = "Ownership is required (FIRST, SECOND, THIRD)")
    private VehicleOwnership ownership;
}
