package com.household.household.service;

import java.math.BigDecimal;

import com.household.household.enums.TransactionType;


 //Service to resolve the annual depreciation rate for AC products.
 //Customer to Customer → 15% per year
 //Dealer to Customer → 10% per year

public interface AcDepreciationRateService {

    BigDecimal getDepreciationRate(TransactionType type);
}
