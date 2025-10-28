package org.example.gavebackend.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.gavebackend.entities.enums.OrderStatus;

@Data
public class ChangeOrderStatusDTO {
    @NotNull
    private OrderStatus status;
}