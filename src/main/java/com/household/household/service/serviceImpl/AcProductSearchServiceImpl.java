package com.household.household.service.serviceImpl;

import com.household.household.dto.response.AcProductResponse;
import com.household.household.entity.AcData;
import com.household.household.enums.AcType;
import com.household.household.enums.AcBrand;
import com.household.household.enums.AcInverterType;
import com.household.household.exception.AcProductNotFoundException;
import com.household.household.repository.AcDataRepository;
import com.household.household.service.AcProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AcProductSearchServiceImpl implements AcProductSearchService {

    private final AcDataRepository acDataRepository;

    @Override
    public AcProductResponse searchProduct(String brandStr, String modelName, String acTypeStr,
                                           BigDecimal capacityInTon, String inverterNonInverterStr,
                                           Integer starRating) {
        
        AcBrand brand;
        AcType acType;
        AcInverterType inverterType;

        try {
            brand = AcBrand.fromString(brandStr);
            acType = AcType.fromString(acTypeStr);
            inverterType = AcInverterType.fromString(inverterNonInverterStr);
        } catch (IllegalArgumentException e) {
            throw new AcProductNotFoundException(e.getMessage());
        }

        AcData acData = acDataRepository.findExactMatch(
                brand, modelName, acType, capacityInTon, inverterType, starRating)
                .orElseThrow(() -> new AcProductNotFoundException("AC Product not found for the given criteria"));

        return AcProductResponse.builder()
                .year(acData.getYear())
                .brand(brandStr)
                .modelName(acData.getModelName())
                .acType(acTypeStr)
                .capacityInTon(acData.getCapacityInTon())
                .inverterNonInverter(inverterNonInverterStr)
                .starRating(acData.getStarRating())
                .launchingPrice(acData.getLaunchingPrice())
                .build();
    }
}
