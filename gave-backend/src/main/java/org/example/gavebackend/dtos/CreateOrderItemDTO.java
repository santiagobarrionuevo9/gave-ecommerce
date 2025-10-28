package org.example.gavebackend.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderItemDTO {
    @NotNull private Long productId;
    @NotNull
    @Min(1) private Integer quantity;
}
