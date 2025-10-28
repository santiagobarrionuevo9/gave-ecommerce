package org.example.gavebackend.dtos;

import lombok.Data;
import org.example.gavebackend.entities.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private String buyerEmail;
    private String buyerName;
    private String buyerPhone;
    private OrderStatus status;
    private BigDecimal itemsTotal;
    private BigDecimal grandTotal;
    private Instant createdAt;
    private Instant updatedAt;
    private List<OrderItemDTO> items;
}
