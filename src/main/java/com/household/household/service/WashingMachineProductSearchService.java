package com.household.household.service;

import com.household.household.dto.response.WashingMachineProductResponse;

import java.math.BigDecimal;

public interface WashingMachineProductSearchService {
    WashingMachineProductResponse searchProduct(String brand, String modelNumber, String washingType,
                                                 BigDecimal capacityKg, String loadingType);
}
