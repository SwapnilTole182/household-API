package com.household.household.repository;

import com.household.household.entity.UsedCarData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface UsedCarDataRepository extends JpaRepository<UsedCarData, Long> {
    Set<String> findRowHashByRowHashIn(Set<String> rowHashes);
}
