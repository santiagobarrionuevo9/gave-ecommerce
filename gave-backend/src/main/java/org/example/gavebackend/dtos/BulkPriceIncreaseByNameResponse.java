package org.example.gavebackend.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BulkPriceIncreaseByNameResponse {
    private String keyword;
    private BigDecimal percent;
    private Integer updatedCount;
}