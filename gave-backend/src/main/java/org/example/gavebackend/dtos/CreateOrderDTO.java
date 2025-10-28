package org.example.gavebackend.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderDTO {
    @Email
    @NotBlank
    private String buyerEmail;
    @NotBlank private String buyerName;
    private String buyerPhone;

    @NotEmpty
    private List<CreateOrderItemDTO> items;
}