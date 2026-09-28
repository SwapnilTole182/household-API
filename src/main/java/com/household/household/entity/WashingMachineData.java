package com.household.household.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.household.household.enums.WashingMachineBrand;
import com.household.household.enums.WashingType;
import com.household.household.enums.WashingMachineLoadingType;

@Entity
@Table(name = "washing_machine_data", indexes = {
        @Index(name = "idx_wm_year_brand_type", columnList = "`Year`, `Brand`, `Washing Type`"),
        @Index(name = "idx_wm_brand", columnList = "`Brand`"),
        @Index(name = "idx_wm_year", columnList = "`Year`")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WashingMachineData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "`Year`")
    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(name = "`Brand`", columnDefinition = "VARCHAR(255)")
    private WashingMachineBrand brand;

    @Column(name = "`Model Number`")
    private String modelNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "`Washing Type`", columnDefinition = "VARCHAR(255)")
    private WashingType washingType;

    @Column(name = "`Capacity (Kg)`")
    private BigDecimal capacityKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "`Loading Type`", columnDefinition = "VARCHAR(255)")
    private WashingMachineLoadingType loadingType;

    @Column(name = "`Launching Price`")
    private BigDecimal launchingPrice;

    @JsonIgnore
    @Column(name = "row_hash", unique = true, length = 64)
    private String rowHash;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
