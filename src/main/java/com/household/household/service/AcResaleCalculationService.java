package com.household.household.service;

import com.household.household.dto.request.AcResaleCalculationRequest;
import com.household.household.dto.response.AcResaleCalculationResponse;
import com.household.household.enums.TransactionType;

 //Service for calculating the depreciation and resale value of AC products.
 //Uses compound year-wise depreciation: Base Value = Original Cost × (1 − r)^t
public interface AcResaleCalculationService {


    AcResaleCalculationResponse calculate(AcResaleCalculationRequest request, TransactionType type);
}
