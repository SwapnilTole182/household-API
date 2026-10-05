package com.household.household.service.serviceImpl;

import com.household.household.dto.response.UsedCarProductResponse;
import com.household.household.entity.UsedCarData;
import com.household.household.enums.CarCompany;
import com.household.household.enums.CarFuelType;
import com.household.household.exception.UsedCarProductNotFoundException;
import com.household.household.repository.UsedCarDataRepository;
import com.household.household.service.UsedCarProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsedCarProductSearchServiceImpl implements UsedCarProductSearchService {

    private final UsedCarDataRepository usedCarDataRepository;

    @Override
    public UsedCarProductResponse searchProduct(String companyStr, String modelName, String variant, String fuelTypeStr) {

        CarCompany company;
        CarFuelType fuelType;

        try {
            company = CarCompany.fromString(companyStr);
            fuelType = CarFuelType.fromString(fuelTypeStr);
        } catch (IllegalArgumentException e) {
            throw new UsedCarProductNotFoundException(e.getMessage());
        }

        UsedCarData usedCarData = usedCarDataRepository
                .findFirstByCompanyAndModelNameIgnoreCaseAndVariantIgnoreCaseAndFuelType(
                        company, modelName, variant, fuelType)
                .orElseThrow(() -> new UsedCarProductNotFoundException(
                        "Used Car product not found for the given criteria"));

        return UsedCarProductResponse.builder()
                .company(companyStr)
                .modelName(usedCarData.getModelName())
                .variant(usedCarData.getVariant())
                .launchYear(usedCarData.getLaunchYear())
                .fuelType(fuelTypeStr)
                .launchingPrice(usedCarData.getLaunchingPrice())
                .build();
    }
}
