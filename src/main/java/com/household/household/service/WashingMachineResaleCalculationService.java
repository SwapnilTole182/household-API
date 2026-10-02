package com.household.household.service;

import com.household.household.dto.request.WashingMachineResaleCalculationRequest;
import com.household.household.dto.response.WashingMachineResaleCalculationResponse;
import com.household.household.enums.TransactionType;

 //
 // Service for calculating the depreciation and resale value of Washing Machine products.
 // Uses compound year-wise depreciation: Base Value = Original Cost × (1 − r)^t

 // Customer to Customer rates:
 //   - FULLY_AUTOMATIC: 18% per year
 //   - SEMI_AUTOMATIC: 15% per year
 //   - WASHER_DRYER: 15% per year

 // Dealer to Customer (resale) rates:
 //   - FULLY_AUTOMATIC: 13% per year
 //   - SEMI_AUTOMATIC: 10% per year
 //   - WASHER_DRYER: 10% per year
public interface WashingMachineResaleCalculationService {

    // Calculates the resale value of a Washing Machine product.
    WashingMachineResaleCalculationResponse calculate(WashingMachineResaleCalculationRequest request, TransactionType type);
}
