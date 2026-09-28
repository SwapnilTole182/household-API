package com.household.household.service;

import java.math.BigDecimal;

import com.household.household.enums.TransactionType;

/**
 * Service to resolve the annual depreciation rate for AC products.
 *
 * Customer to Customer → 15% per year
 * Dealer to Customer → 10% per year
 */
public interface AcDepreciationRateService {

    /**
     * Returns the annual depreciation rate based on transaction type.
     *
     * @param type the transaction type (e.g., CUSTOMER_TO_CUSTOMER)
     * @return the depreciation rate as a BigDecimal (e.g., 0.15 for 15%)
     */
    BigDecimal getDepreciationRate(TransactionType type);
}
