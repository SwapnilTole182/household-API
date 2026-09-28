package com.household.household.enums;

import java.math.BigDecimal;

/**
 * Enum-based depreciation rate configuration for AC products.
 *
 * Customer to Customer → 15% per year
 * Dealer to Customer → 10% per year
 */
public enum AcDepreciationRate {

    CUSTOMER_TO_CUSTOMER(new BigDecimal("0.15")),
    DEALER_TO_CUSTOMER(new BigDecimal("0.10"));

    private final BigDecimal rate;

    AcDepreciationRate(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getRate() {
        return rate;
    }

    /**
     * Resolves the depreciation rate based on TransactionType.
     *
     * @param type the TransactionType
     * @return the matching AcDepreciationRate
     */
    public static AcDepreciationRate resolve(TransactionType type) {
        switch (type) {
            case DEALER_TO_CUSTOMER:
                return DEALER_TO_CUSTOMER;
            case CUSTOMER_TO_CUSTOMER:
            default:
                return CUSTOMER_TO_CUSTOMER;
        }
    }
}
