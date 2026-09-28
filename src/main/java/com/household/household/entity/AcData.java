package com.household.household.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.household.household.enums.AcType;
import com.household.household.enums.AcBrand;
import com.household.household.enums.AcInverterType;

@Entity
@Table(name = "ac_data", indexes = {
        @Index(name = "idx_ac_year_brand_type", columnList = "`Year`, `Brand`, `Ac Type`"),
        @Index(name = "idx_ac_brand", columnList = "`Brand`"),
        @Index(name = "idx_ac_year", columnList = "`Year`")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "`Year`")
    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(name = "`Brand`", columnDefinition = "VARCHAR(255)")
    private AcBrand brand;

    @Column(name = "`Model Name`")
    private String modelName;

    @Enumerated(EnumType.STRING)
    @Column(name = "`Ac Type`", columnDefinition = "VARCHAR(255)")
    private AcType acType;

    @Column(name = "`Capacity in Ton`")
    private BigDecimal capacityInTon;

    @Enumerated(EnumType.STRING)
    @Column(name = "`Inverter/Non-Inverter`", columnDefinition = "VARCHAR(255)")
    private AcInverterType inverterNonInverter;

    @Column(name = "`Star Rating`")
    private Integer starRating;

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
