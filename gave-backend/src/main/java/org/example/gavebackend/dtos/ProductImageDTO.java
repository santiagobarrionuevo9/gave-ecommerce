package org.example.gavebackend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductImageDTO {
    private Long id;
    @NotNull
    private Long productId;
    @NotBlank
    private String url;
    private String altText;
    private Integer sortOrder;

    // NUEVO: guardar el public_id de Cloudinary para poder borrar
    private String cloudPublicId;
}
