package com.household.household.repository;

import com.household.household.entity.WashingMachineData;
import com.household.household.enums.WashingMachineBrand;
import com.household.household.enums.WashingType;
import com.household.household.enums.WashingMachineLoadingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

@Repository
public interface WashingMachineDataRepository extends JpaRepository<WashingMachineData, Long> {

    @Query("SELECT w.rowHash FROM WashingMachineData w WHERE w.rowHash IN :hashes")
    Set<String> findRowHashByRowHashIn(@Param("hashes") Set<String> hashes);

    /**
     * Finds an exact matching Washing Machine product.
     * ModelNumber is compared case-insensitively using LOWER().
     * Enum and numeric fields use direct equality.
     */
    @Query("SELECT w FROM WashingMachineData w WHERE " +
            "w.brand = :brand " +
            "AND LOWER(w.modelNumber) = LOWER(:modelNumber) " +
            "AND w.washingType = :washingType " +
            "AND w.capacityKg = :capacityKg " +
            "AND w.loadingType = :loadingType " +
            "ORDER BY w.year DESC LIMIT 1")
    Optional<WashingMachineData> findExactMatch(@Param("brand") WashingMachineBrand brand,
                                                 @Param("modelNumber") String modelNumber,
                                                 @Param("washingType") WashingType washingType,
                                                 @Param("capacityKg") BigDecimal capacityKg,
                                                 @Param("loadingType") WashingMachineLoadingType loadingType);
}
