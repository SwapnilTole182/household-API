package com.household.household.service.serviceImpl;

import com.household.household.enums.AcDepreciationRate;
import com.household.household.enums.TransactionType;
import com.household.household.service.AcDepreciationRateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class AcDepreciationRateServiceImpl implements AcDepreciationRateService {

    @Override
    public BigDecimal getDepreciationRate(TransactionType type) {
        AcDepreciationRate depreciationRate = AcDepreciationRate.resolve(type);

        log.info("Resolved depreciation rate for transaction type '{}': {}%",
                type,
                depreciationRate.getRate().multiply(new BigDecimal("100")).stripTrailingZeros());

        return depreciationRate.getRate();
    }
}
