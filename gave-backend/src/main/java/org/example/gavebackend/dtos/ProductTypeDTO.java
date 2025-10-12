package org.example.gavebackend.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductTypeDTO {
    private Long id;
    @NotBlank private String name;
    @NotBlank private String slug;
    private String description;
}
