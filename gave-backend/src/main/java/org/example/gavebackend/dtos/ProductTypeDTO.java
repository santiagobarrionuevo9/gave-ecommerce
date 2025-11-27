package org.example.gavebackend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductTypeDTO {
    private Long id;

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

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    private String description;
}
