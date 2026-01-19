package org.example.gavebackend.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BulkPriceIncreaseByNameRequest {
    @NotBlank
    private String keyword; // ej: "WEGA", "Pro Filter", "MANN"

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true)
    private BigDecimal percent; // ej: 10 = +10%

    // opcional: por defecto solo activos
    private Boolean activeOnly = true;
}
