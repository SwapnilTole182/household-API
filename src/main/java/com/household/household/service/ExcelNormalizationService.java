package com.household.household.service;

import com.household.household.entity.AcData;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ExcelNormalizationService {

    public AcData normalize(AcData data) {
        if (data == null) {
            return null;
        }


        data.setModelName(normalizeString(data.getModelName()));

        data.setCapacityInTon(normalizeNumeric(data.getCapacityInTon()));
        data.setLaunchingPrice(normalizeNumeric(data.getLaunchingPrice()));

        return data;
    }

    public com.household.household.entity.UsedCarData normalize(com.household.household.entity.UsedCarData data) {
        if (data == null) {
            return null;
        }

        data.setModelName(normalizeString(data.getModelName()));
        data.setVariant(normalizeString(data.getVariant()));
        data.setLaunchingPrice(normalizeNumeric(data.getLaunchingPrice()));

        return data;
    }

    private String normalizeString(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    private BigDecimal normalizeNumeric(BigDecimal value) {
        if (value == null) {
            return null;
        }
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return value.stripTrailingZeros();
    }
}
