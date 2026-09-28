package com.household.household.controller;

import com.household.household.dto.request.AcResaleCalculationRequest;
import com.household.household.dto.response.AcResaleCalculationResponse;
import com.household.household.enums.TransactionType;
import com.household.household.service.AcResaleCalculationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ac/depreciation")
@RequiredArgsConstructor
public class AcResaleCalculationController {

    private final AcResaleCalculationService calculationService;

    /**
     * Calculates the depreciation and resale value of an AC product.
     *
     * POST /api/ac/depreciation/calculate
     *
     * Uses compound year-wise depreciation: Resale Value = Original Cost × (1 − r)^t
     * The launching price is always retrieved from the database, never from the request.
     *
     *  This API is used to when customer selling the ac to dealer or other customer
     */

    //Selling AC From Customer to Customer :
    @PostMapping("/customer/calculate")
    public ResponseEntity<AcResaleCalculationResponse> calculateDepreciation(
            @Valid @RequestBody AcResaleCalculationRequest request) {

        AcResaleCalculationResponse response = calculationService.calculate(request, TransactionType.CUSTOMER_TO_CUSTOMER);
        return ResponseEntity.ok(response);
    }

    //Selling AC From Dealer to Customer :
    @PostMapping("/dealer/calculate")
    public ResponseEntity<AcResaleCalculationResponse> calculateDealerDepreciation(
            @Valid @RequestBody AcResaleCalculationRequest request) {

        AcResaleCalculationResponse response = calculationService.calculate(request, TransactionType.DEALER_TO_CUSTOMER);
        return ResponseEntity.ok(response);
    }

}
