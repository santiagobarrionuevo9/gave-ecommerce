package org.example.gavebackend.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class ProductDTO {

    private Long id;

    @NotNull(message = "El tipo de producto es requerido")
    private Long typeId;

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String name;

    @NotBlank(message = "El slug es requerido")
    @Size(max = 120, message = "El slug no puede superar los 120 caracteres")
    @Pattern(
            regexp = "^[a-z0-9-]+$",
            message = "El slug solo puede contener letras minúsculas, números y guiones"
    )
    private String slug;

    @Size(max = 255, message = "La descripción corta no puede superar los 255 caracteres")
    private String shortDesc;

    @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
    private String description;

    private Boolean isActive;

    @NotBlank(message = "SKU es requerido")
    @Size(max = 50, message = "El SKU no puede superar los 50 caracteres")
    private String sku;

    @NotNull(message = "El precio es requerido")
    @DecimalMin(value = "0.00", inclusive = true, message = "El precio no puede ser negativo")

    private BigDecimal price;

    @NotNull(message = "El stock es requerido")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @Min(value = 0, message = "El umbral de descuento no puede ser negativo")
    private Integer discountThreshold;

    @DecimalMin(value = "0.00", inclusive = true, message = "El porcentaje de descuento no puede ser negativo")
    @DecimalMax(value = "100.00", inclusive = true, message = "El porcentaje de descuento no puede superar 100")
    private BigDecimal discountPercent;

    private Integer availableStock;
    private String stockLevel;

    private Integer stockLowThreshold;


    private Integer stockMediumThreshold;

    private Instant createdAt;
}
