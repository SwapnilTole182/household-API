package com.household.household.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcResaleCalculationResponse {

    private AcProductResponse product;
    private AcDepreciationCalculationResponse calculation;
}
