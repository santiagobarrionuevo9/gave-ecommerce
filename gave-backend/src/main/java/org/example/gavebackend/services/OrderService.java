package org.example.gavebackend.services;

import org.example.gavebackend.dtos.ChangeOrderStatusDTO;
import org.example.gavebackend.dtos.CreateOrderDTO;
import org.example.gavebackend.dtos.OrderDTO;
import org.example.gavebackend.entities.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderDTO createOrder(CreateOrderDTO dto);
    OrderDTO get(Long id);
    Page<OrderDTO> myOrders(String buyerEmail, Pageable pg);
    Page<OrderDTO> listByStatus(OrderStatus status, Pageable pg);
    OrderDTO changeStatus(Long id, ChangeOrderStatusDTO dto); // ADMIN
}