package org.example.gavebackend.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class ProductDTO {
    private Long id;

    @NotNull private Long typeId;

    @NotBlank private String name;
    @NotBlank private String slug;

    private String shortDesc;
    private String description;
    private Boolean isActive;

    // venta
    @NotBlank(message = "SKU es requerido") private String sku;
    @NotNull @DecimalMin("0.00") private BigDecimal price;
    @NotNull @Min(0) private Integer stock;
    // 👇 NUEVO
    private Instant createdAt;
}
