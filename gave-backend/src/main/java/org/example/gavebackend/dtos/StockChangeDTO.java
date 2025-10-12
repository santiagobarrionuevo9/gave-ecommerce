package org.example.gavebackend.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockChangeDTO {
    @NotNull
    private Integer delta; // + agrega, - descuenta
}