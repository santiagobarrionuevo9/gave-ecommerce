package org.example.gavebackend.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.gavebackend.entities.enums.DeliveryMethod;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderDTO {
    @Email
    @NotBlank
    private String buyerEmail;
    @NotBlank private String buyerName;
    private String buyerPhone;

    @NotNull
    private DeliveryMethod deliveryMethod;   // DELIVERY o PICKUP
    private ShippingAddressDTO address;               // obligatorio si DELIVERY
    private BigDecimal deliveryCost;                  // 0 si PICKUP

    @NotEmpty
    private List<CreateOrderItemDTO> items;
}