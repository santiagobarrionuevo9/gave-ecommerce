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

    private String cloudPublicId;
}
