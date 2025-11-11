package org.example.gavebackend.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockChangeDTOItem {
    @NotNull
    private Long productId;
    @NotNull private StockChangeDTO.Op operation;
    @NotNull @Min(0) private Integer amount;
    private String reason;
}