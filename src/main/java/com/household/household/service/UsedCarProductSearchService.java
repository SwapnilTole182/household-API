package com.household.household.service;

import com.household.household.dto.response.UsedCarProductResponse;

public interface UsedCarProductSearchService {

    UsedCarProductResponse searchProduct(String company, String modelName, String variant, String fuelType);
}
