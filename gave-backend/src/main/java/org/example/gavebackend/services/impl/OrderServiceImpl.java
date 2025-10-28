package org.example.gavebackend.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.gavebackend.dtos.*;
import org.example.gavebackend.entities.enums.OrderStatus;
import org.example.gavebackend.entities.order;
import org.example.gavebackend.entities.orderItem;
import org.example.gavebackend.entities.product;
import org.example.gavebackend.repositories.orderRepository;
import org.example.gavebackend.repositories.productRepository;
import org.example.gavebackend.services.MailService;
import org.example.gavebackend.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    @Autowired
    private orderRepository orderRepo;
    @Autowired
    private productRepository productRepo;
    @Autowired
    private MailService mail;

    @Override @Transactional
    public OrderDTO createOrder(CreateOrderDTO dto) {
        if (dto.getItems()==null || dto.getItems().isEmpty())
            throw new IllegalArgumentException("El pedido no tiene items");

        order order = new order();
        order.setBuyerEmail(dto.getBuyerEmail().trim().toLowerCase());
        order.setBuyerName(dto.getBuyerName().trim());
        order.setBuyerPhone(dto.getBuyerPhone());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal itemsTotal = BigDecimal.ZERO;

        // construir items con lock y reservas
        for (CreateOrderItemDTO it : dto.getItems()) {
            product p = productRepo.lockById(it.getProductId());
            if (p == null || p.getIsActive()==null || !p.getIsActive())
                throw new IllegalArgumentException("Producto inválido: " + it.getProductId());

            int qty = it.getQuantity();
            if (qty <= 0) throw new IllegalArgumentException("Cantidad inválida");

            int available = p.getStock() - (p.getReserved()==null?0:p.getReserved());
            if (available < qty)
                throw new IllegalArgumentException("Sin stock disponible para " + p.getName());

            // reservar
            p.setReserved((p.getReserved()==null?0:p.getReserved()) + qty);
            productRepo.save(p);

            // item con precio actual
            BigDecimal unit = p.getPrice();
            BigDecimal line = unit.multiply(BigDecimal.valueOf(qty));

            orderItem oi = new orderItem();
            oi.setOrder(order);
            oi.setProduct(p);
            oi.setQuantity(qty);
            oi.setUnitPrice(unit);
            oi.setLineTotal(line);

            order.getItems().add(oi);
            itemsTotal = itemsTotal.add(line);
        }

        order.setItemsTotal(itemsTotal);
        order.setGrandTotal(itemsTotal); // sin delivery/recargos por ahora

        order = orderRepo.save(order);

        // email 1 sola vez al crear
        try {
            mail.sendOrderConfirmationEmail(order.getBuyerEmail(), order.getBuyerName(), order.getId(), order.getGrandTotal());
        } catch (Exception ignored) {}

        return toDTO(order);
    }

    @Override
    public OrderDTO get(Long id) {
        return orderRepo.findById(id).map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
    }

    @Override
    public Page<OrderDTO> myOrders(String buyerEmail, Pageable pg) {
        return orderRepo.findByBuyerEmailOrderByCreatedAtDesc(buyerEmail.trim().toLowerCase(), pg).map(this::toDTO);
    }

    @Override
    public Page<OrderDTO> listByStatus(OrderStatus status, Pageable pg) {
        return orderRepo.findByStatusOrderByCreatedAtDesc(status, pg).map(this::toDTO);
    }

    @Override @Transactional
    public OrderDTO changeStatus(Long id, ChangeOrderStatusDTO dto) {
        order order = orderRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        OrderStatus from = order.getStatus();
        OrderStatus to   = dto.getStatus();

        if (from == to) return toDTO(order);

        // transiciones simples (podés endurecer reglas)
        switch (to) {
            case CANCELED -> releaseReservations(order);          // libera reserva
            case DELIVERED -> finalizeDelivery(order);            // descuenta stock y reserva
            default -> { /* no tocar stock/reservas */ }
        }

        order.setStatus(to);
        return toDTO(orderRepo.save(order));
    }

    /* === helpers stock/reservas === */

    private void releaseReservations(order order) {
        order.getItems().forEach(oi -> {
            product p = productRepo.lockById(oi.getProduct().getId());
            int reserved = p.getReserved()==null?0:p.getReserved();
            p.setReserved(Math.max(0, reserved - oi.getQuantity()));
            productRepo.save(p);
        });
    }

    private void finalizeDelivery(order order) {
        order.getItems().forEach(oi -> {
            product p = productRepo.lockById(oi.getProduct().getId());
            int reserved = p.getReserved()==null?0:p.getReserved();
            p.setReserved(Math.max(0, reserved - oi.getQuantity()));
            p.setStock(p.getStock() - oi.getQuantity()); // ahora sí descuenta stock real
            productRepo.save(p);
        });
    }

    /* === mapper === */

    private OrderDTO toDTO(order o){
        OrderDTO d = new OrderDTO();
        d.setId(o.getId());
        d.setBuyerEmail(o.getBuyerEmail());
        d.setBuyerName(o.getBuyerName());
        d.setBuyerPhone(o.getBuyerPhone());
        d.setStatus(o.getStatus());
        d.setItemsTotal(o.getItemsTotal());
        d.setGrandTotal(o.getGrandTotal());
        d.setCreatedAt(o.getCreatedAt());
        d.setUpdatedAt(o.getUpdatedAt());
        d.setItems(o.getItems().stream().map(oi -> {
            OrderItemDTO x = new OrderItemDTO();
            x.setId(oi.getId());
            x.setProductId(oi.getProduct().getId());
            x.setProductName(oi.getProduct().getName());
            x.setQuantity(oi.getQuantity());
            x.setUnitPrice(oi.getUnitPrice());
            x.setLineTotal(oi.getLineTotal());
            return x;
        }).toList());
        return d;
    }
}
