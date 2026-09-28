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
public class AcProductResponse {

    private Integer year;
    private String brand;
    private String modelName;
    private String acType;
    private BigDecimal capacityInTon;
    private String inverterNonInverter;
    private Integer starRating;
    private BigDecimal launchingPrice;
}
