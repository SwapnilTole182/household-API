package com.household.household.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.household.household.enums.CarCompany;
import com.household.household.enums.CarFuelType;

@Entity
@Table(name = "used_car_data", indexes = {
        @Index(name = "idx_used_car_year_company_model", columnList = "`Launch Year`, `Company`, `Model Name`"),
        @Index(name = "idx_used_car_company", columnList = "`Company`"),
        @Index(name = "idx_used_car_year", columnList = "`Launch Year`")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsedCarData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "`Company`", columnDefinition = "VARCHAR(255)")
    private CarCompany company;

    @Column(name = "`Model Name`")
    private String modelName;

    @Column(name = "`Variant`")
    private String variant;

    @Column(name = "`Launch Year`")
    private Integer launchYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "`Fuel Type`", columnDefinition = "VARCHAR(255)")
    private CarFuelType fuelType;

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
