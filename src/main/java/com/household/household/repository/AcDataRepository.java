package com.household.household.repository;

import com.household.household.entity.AcData;
import com.household.household.enums.AcType;
import com.household.household.enums.AcBrand;
import com.household.household.enums.AcInverterType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

@Repository
public interface AcDataRepository extends JpaRepository<AcData, Long> {

    @Query("SELECT a.rowHash FROM AcData a WHERE a.rowHash IN :hashes")
    Set<String> findRowHashByRowHashIn(@Param("hashes") Set<String> hashes);

    /**
     * Finds an exact matching AC product.
     * Brand and modelName are compared case-insensitively using LOWER().
     * Enum and numeric fields use direct equality.
     */
    @Query("SELECT a FROM AcData a WHERE " +
            "a.brand = :brand " +
            "AND LOWER(a.modelName) = LOWER(:modelName) " +
            "AND a.acType = :acType " +
            "AND a.capacityInTon = :capacityInTon " +
            "AND a.inverterNonInverter = :inverterNonInverter " +
            "AND a.starRating = :starRating " +
            "ORDER BY a.year DESC LIMIT 1")
    Optional<AcData> findExactMatch(@Param("brand") AcBrand brand,
                                    @Param("modelName") String modelName,
                                    @Param("acType") AcType acType,
                                    @Param("capacityInTon") BigDecimal capacityInTon,
                                    @Param("inverterNonInverter") AcInverterType inverterNonInverter,
                                    @Param("starRating") Integer starRating);
}
