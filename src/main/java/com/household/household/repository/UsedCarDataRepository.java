package com.household.household.repository;

import com.household.household.entity.UsedCarData;
import com.household.household.enums.CarCompany;
import com.household.household.enums.CarFuelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface UsedCarDataRepository extends JpaRepository<UsedCarData, Long> {
    Set<String> findRowHashByRowHashIn(Set<String> rowHashes);

    Optional<UsedCarData> findFirstByCompanyAndModelNameIgnoreCaseAndVariantIgnoreCaseAndFuelType(
            CarCompany company, String modelName, String variant, CarFuelType fuelType);
}
