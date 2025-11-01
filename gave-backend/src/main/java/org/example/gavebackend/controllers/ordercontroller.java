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
    @Autowired private OrderService orders;
    @Autowired private JwtUtil jwt;

    @PostMapping                 // POST /api/orders
    public OrderDTO create(@Valid @RequestBody CreateOrderDTO dto) {
        return orders.createOrder(dto);
    }

    @GetMapping("/mine")         // GET /api/orders/mine?buyerEmail=...
    public Page<OrderDTO> mine(@RequestParam(defaultValue="0") int page,
                               @RequestParam(defaultValue="10") int size,
                               @RequestParam String buyerEmail) {
        return orders.myOrders(buyerEmail, PageRequest.of(page, size));
    }

    @GetMapping("/{id}")         // GET /api/orders/{id}
    public OrderDTO get(@PathVariable Long id){ return orders.get(id); }

    // ADMIN
    @GetMapping("/admin")        // GET /api/orders/admin?status=...
    public Page<OrderDTO> listByStatus(@RequestParam OrderStatus status,
                                       @RequestParam(defaultValue="0") int page,
                                       @RequestParam(defaultValue="10") int size){
        return orders.listByStatus(status, PageRequest.of(page, size));
    }

    @PutMapping("/admin/{id}/status") // PUT /api/orders/admin/{id}/status
    public OrderDTO changeStatus(@PathVariable Long id, @Valid @RequestBody ChangeOrderStatusDTO dto){
        return orders.changeStatus(id, dto);
    }
}

