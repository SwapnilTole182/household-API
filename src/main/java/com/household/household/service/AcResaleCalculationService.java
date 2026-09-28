package com.household.household.service;

import com.household.household.dto.request.AcResaleCalculationRequest;
import com.household.household.dto.response.AcResaleCalculationResponse;
import com.household.household.enums.TransactionType;

/**
 * Service for calculating the depreciation and resale value of AC products.
 * Uses compound year-wise depreciation: Base Value = Original Cost × (1 − r)^t
 */
public interface AcResaleCalculationService {

    /**
     * Calculates the resale value of an AC product.
     *
     * @param request the calculation request containing product identification fields
     * @param type the transaction type (e.g. CUSTOMER_TO_CUSTOMER or DEALER_TO_CUSTOMER)
     * @return the calculation response with product info and depreciation breakdown
     */
    AcResaleCalculationResponse calculate(AcResaleCalculationRequest request, TransactionType type);
}
