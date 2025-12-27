package org.example.gavebackend.dtos;

import lombok.Data;

@Data
public class BulkDiscountByNameResponse {
    private String keyword;
    private Integer updatedCount;
}
