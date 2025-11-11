package org.example.gavebackend.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockChangeDTO {
    public enum Op { SET, INCREMENT, DECREMENT }

    @NotNull private Op operation;  // SET | INCREMENT | DECREMENT
    @NotNull @Min(0) private Integer amount; // para SET = nuevo valor; para +/- = delta
    private String reason;
}