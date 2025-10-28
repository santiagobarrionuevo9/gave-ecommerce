package org.example.gavebackend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.gavebackend.dtos.ChangeOrderStatusDTO;
import org.example.gavebackend.dtos.CreateOrderDTO;
import org.example.gavebackend.dtos.OrderDTO;
import org.example.gavebackend.entities.enums.OrderStatus;
import org.example.gavebackend.services.JwtUtil;
import org.example.gavebackend.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class ordercontroller {
    @Autowired
    private OrderService orders;
    @Autowired
    private  JwtUtil jwt;

    // Cliente crea pedido (usa dto con items)
    @PostMapping("/orders")
    public OrderDTO create(@Valid @RequestBody CreateOrderDTO dto){
        // si tenés autenticación JWT, podés forzar buyerEmail = del token
        // String email = jwt.getEmailFromToken(... SecurityContext ...);
        return orders.createOrder(dto);
    }

    // Cliente ve sus pedidos (paginado)
    @GetMapping("/orders/mine")
    public Page<OrderDTO> mine(@RequestParam(defaultValue="0") int page,
                               @RequestParam(defaultValue="10") int size,
                               @RequestParam String buyerEmail){
        // si estás autenticado, derivá buyerEmail del token y no lo aceptes por query
        return orders.myOrders(buyerEmail, PageRequest.of(page, size));
    }

    // ADMIN: listar por estado (paginado)
    @GetMapping("/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<OrderDTO> listByStatus(@RequestParam OrderStatus status,
                                       @RequestParam(defaultValue="0") int page,
                                       @RequestParam(defaultValue="10") int size){
        return orders.listByStatus(status, PageRequest.of(page, size));
    }

    // ADMIN: cambiar estado
    @PutMapping("/admin/orders/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public OrderDTO changeStatus(@PathVariable Long id, @Valid @RequestBody ChangeOrderStatusDTO dto){
        return orders.changeStatus(id, dto);
    }

    // Detalle pedido (podés restringir a dueño o admin)
    @GetMapping("/orders/{id}")
    public OrderDTO get(@PathVariable Long id){
        return orders.get(id);
    }
}

