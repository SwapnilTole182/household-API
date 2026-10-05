package com.household.household.service;

import com.household.household.dto.request.UsedCarResaleCalculationRequest;
import com.household.household.dto.response.UsedCarResaleCalculationResponse;
import com.household.household.enums.TransactionType;

/**
 * Service for calculating the depreciation and resale value of Used Car products.
 *
 * Uses:
 * - 60% depreciation in the first year
 * - 1% compound depreciation per year for remaining years
 * - Condition multiplier (GOOD / FAIR / POOR)
 * - KM deduction (0.5% per 10,000 km driven)
 * - Ownership deduction (1% per owner number)
 */
public interface UsedCarResaleCalculationService {

    UsedCarResaleCalculationResponse calculate(UsedCarResaleCalculationRequest request, TransactionType type);
}
