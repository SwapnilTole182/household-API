package com.household.household.controller;

import com.household.household.dto.request.WashingMachineResaleCalculationRequest;
import com.household.household.dto.response.WashingMachineResaleCalculationResponse;
import com.household.household.enums.TransactionType;
import com.household.household.service.WashingMachineResaleCalculationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/washing-machine/depreciation")
@RequiredArgsConstructor
public class WashingMachineResaleCalculationController {

    private final WashingMachineResaleCalculationService calculationService;

    /**
     * Calculates the depreciation and resale value of a Washing Machine product.
     *
     * Uses compound year-wise depreciation: Resale Value = Original Cost × (1 − r)^t
     *
     * Customer to Customer depreciation rates:
     *   - FULLY_AUTOMATIC: 18% per year
     *   - SEMI_AUTOMATIC: 15% per year
     *   - WASHER_DRYER: 15% per year
     *
     * Dealer to Customer (resale) depreciation rates:
     *   - FULLY_AUTOMATIC: 13% per year
     *   - SEMI_AUTOMATIC: 10% per year
     *   - WASHER_DRYER: 10% per year
     *
     * The launching price is always retrieved from the database, never from the request.
     */

    //Customer To Customer
    @PostMapping("/customer/calculate")
    public ResponseEntity<WashingMachineResaleCalculationResponse> calculateDepreciation(
            @Valid @RequestBody WashingMachineResaleCalculationRequest request) {

        WashingMachineResaleCalculationResponse response = calculationService.calculate(request, TransactionType.CUSTOMER_TO_CUSTOMER);
        return ResponseEntity.ok(response);
    }

    //Dealer To Customer
    @PostMapping("/dealer/calculate")
    public ResponseEntity<WashingMachineResaleCalculationResponse> calculateDealerDepreciation(
            @Valid @RequestBody WashingMachineResaleCalculationRequest request) {

        WashingMachineResaleCalculationResponse response = calculationService.calculate(request, TransactionType.DEALER_TO_CUSTOMER);
        return ResponseEntity.ok(response);
    }
}
