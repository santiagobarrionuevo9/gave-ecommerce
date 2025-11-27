package org.example.gavebackend.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.example.gavebackend.entities.enums.DeliveryMethod;
import org.example.gavebackend.entities.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    @NotBlank(message = "El mail es requerido")
    private String buyerEmail;
    @NotBlank(message = "El nombre es requerido")
    private String buyerName;
    @NotBlank(message = "El teléfono es requerido")
    private String buyerPhone;
    private OrderStatus status;
    private DeliveryMethod deliveryMethod;
    private ShippingAddressDTO address;
    private BigDecimal itemsTotal;
    private BigDecimal grandTotal;
    private BigDecimal deliveryCost;
    private Instant createdAt;
    private Instant updatedAt;
    private List<OrderItemDTO> items;
}
