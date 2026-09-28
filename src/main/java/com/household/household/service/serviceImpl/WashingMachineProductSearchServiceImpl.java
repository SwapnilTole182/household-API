package com.household.household.service.serviceImpl;

import com.household.household.dto.response.WashingMachineProductResponse;
import com.household.household.entity.WashingMachineData;
import com.household.household.enums.WashingMachineBrand;
import com.household.household.enums.WashingType;
import com.household.household.enums.WashingMachineLoadingType;
import com.household.household.exception.WashingMachineProductNotFoundException;
import com.household.household.repository.WashingMachineDataRepository;
import com.household.household.service.WashingMachineProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WashingMachineProductSearchServiceImpl implements WashingMachineProductSearchService {

    private final WashingMachineDataRepository washingMachineDataRepository;

    @Override
    public WashingMachineProductResponse searchProduct(String brandStr, String modelNumber, String washingTypeStr,
                                                        BigDecimal capacityKg, String loadingTypeStr) {

        WashingMachineBrand brand;
        WashingType washingType;
        WashingMachineLoadingType loadingType;

        try {
            brand = WashingMachineBrand.fromString(brandStr);
            washingType = WashingType.fromString(washingTypeStr);
            loadingType = WashingMachineLoadingType.fromString(loadingTypeStr);
        } catch (IllegalArgumentException e) {
            throw new WashingMachineProductNotFoundException(e.getMessage());
        }

        WashingMachineData wmData = washingMachineDataRepository.findExactMatch(
                brand, modelNumber, washingType, capacityKg, loadingType)
                .orElseThrow(() -> new WashingMachineProductNotFoundException(
                        "Washing Machine Product not found for the given criteria"));

        return WashingMachineProductResponse.builder()
                .year(wmData.getYear())
                .brand(brandStr)
                .modelNumber(wmData.getModelNumber())
                .washingType(washingTypeStr)
                .capacityKg(wmData.getCapacityKg())
                .loadingType(loadingTypeStr)
                .launchingPrice(wmData.getLaunchingPrice())
                .build();
    }
}
