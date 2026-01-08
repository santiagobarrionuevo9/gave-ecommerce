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
    private String keyword;

    @Min(0)
    private Integer discountThreshold;

    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    private BigDecimal discountPercent;

    private Boolean activeOnly = true;
}