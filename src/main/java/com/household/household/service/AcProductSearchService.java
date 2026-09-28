package com.household.household.service;

import com.household.household.dto.response.AcProductResponse;

import java.math.BigDecimal;

public interface AcProductSearchService {
    AcProductResponse searchProduct(String brand, String modelName, String acType,
                                    BigDecimal capacityInTon, String inverterNonInverter,
                                    Integer starRating);
}
