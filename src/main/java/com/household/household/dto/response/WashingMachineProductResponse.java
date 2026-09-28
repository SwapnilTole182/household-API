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
public class WashingMachineProductResponse {

    private Integer year;
    private String brand;
    private String modelNumber;
    private String washingType;
    private BigDecimal capacityKg;
    private String loadingType;
    private BigDecimal launchingPrice;
}
