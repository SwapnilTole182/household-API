package com.household.household.controller;

import com.household.household.dto.request.UsedCarResaleCalculationRequest;
import com.household.household.dto.response.UsedCarResaleCalculationResponse;
import com.household.household.enums.TransactionType;
import com.household.household.service.UsedCarResaleCalculationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/used-car/depreciation")
@RequiredArgsConstructor
public class UsedCarResaleCalculationController {

    private final UsedCarResaleCalculationService calculationService;

    /**
     * Calculates the depreciation and on-road resale value of a Used Car.
     *
     * POST /api/used-car/depreciation/customer/calculate
     *
     * Uses:
     * - 60% depreciation in the first year
     * - 1% compound depreciation per year for remaining years
     * - Condition multiplier (GOOD / FAIR / POOR)
     * - KM deduction: 0.5% per 10,000 km driven
     * - Ownership deduction: 1% per owner number (1st=1%, 2nd=2%, 3rd=3%)
     *
     * The launching price is always retrieved from the database, never from the request.
     *
     * This API is used when a customer is selling a used car to another customer.
     */

    // Selling Used Car From Customer to Customer :
    @PostMapping("/customer/calculate")
    public ResponseEntity<UsedCarResaleCalculationResponse> calculateDepreciation(
            @Valid @RequestBody UsedCarResaleCalculationRequest request) {

        UsedCarResaleCalculationResponse response = calculationService.calculate(request, TransactionType.CUSTOMER_TO_CUSTOMER);
        return ResponseEntity.ok(response);
    }

    // Selling Used Car From Dealer to Customer :
    @PostMapping("/dealer/calculate")
    public ResponseEntity<UsedCarResaleCalculationResponse> calculateDealerDepreciation(
            @Valid @RequestBody UsedCarResaleCalculationRequest request) {

        UsedCarResaleCalculationResponse response = calculationService.calculate(request, TransactionType.DEALER_TO_CUSTOMER);
        return ResponseEntity.ok(response);
    }
}
