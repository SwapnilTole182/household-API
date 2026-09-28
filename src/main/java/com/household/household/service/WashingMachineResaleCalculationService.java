package com.household.household.service;

import com.household.household.dto.request.WashingMachineResaleCalculationRequest;
import com.household.household.dto.response.WashingMachineResaleCalculationResponse;
import com.household.household.enums.TransactionType;

/**
 * Service for calculating the depreciation and resale value of Washing Machine products.
 * Uses compound year-wise depreciation: Base Value = Original Cost × (1 − r)^t
 *
 * Customer to Customer rates:
 *   - FULLY_AUTOMATIC: 18% per year
 *   - SEMI_AUTOMATIC: 15% per year
 *   - WASHER_DRYER: 15% per year
 *
 * Dealer to Customer (resale) rates:
 *   - FULLY_AUTOMATIC: 13% per year
 *   - SEMI_AUTOMATIC: 10% per year
 *   - WASHER_DRYER: 10% per year
 */
public interface WashingMachineResaleCalculationService {

    /**
     * Calculates the resale value of a Washing Machine product.
     *
     * @param request the calculation request containing product identification fields and condition
     * @param type the transaction type (CUSTOMER_TO_CUSTOMER or DEALER_TO_CUSTOMER)
     * @return the calculation response with product info and depreciation breakdown
     */
    WashingMachineResaleCalculationResponse calculate(WashingMachineResaleCalculationRequest request, TransactionType type);
}
