package org.example.gavebackend.dtos;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BulkDiscountByNameRequest {
    @NotBlank
    private String keyword;            // ej "chevrolet"

    @Min(0)
    private Integer discountThreshold; // null = no tocar

    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    private BigDecimal discountPercent; // null = no tocar

    private Boolean activeOnly = true; // default true
}