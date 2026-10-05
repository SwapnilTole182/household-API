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
public class UsedCarProductResponse {

    private String company;
    private String modelName;
    private String variant;
    private Integer launchYear;
    private String fuelType;
    private BigDecimal launchingPrice;
}
