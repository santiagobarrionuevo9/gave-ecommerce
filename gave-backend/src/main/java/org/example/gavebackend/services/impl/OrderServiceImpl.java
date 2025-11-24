package org.example.gavebackend.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.gavebackend.dtos.*;
import org.example.gavebackend.entities.ShippingAddress;
import org.example.gavebackend.entities.enums.DeliveryMethod;
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

    private static final int DEFAULT_DISCOUNT_QTY = 10;
    private static final BigDecimal DEFAULT_DISCOUNT_PERCENT = new BigDecimal("10.00");

    @Override
    @Transactional
    public OrderDTO createOrder(CreateOrderDTO dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty())
            throw new IllegalArgumentException("El pedido no tiene items");

        if (dto.getDeliveryMethod() == null)
            throw new IllegalArgumentException("deliveryMethod es requerido");

        if (dto.getDeliveryMethod() == DeliveryMethod.DELIVERY) {
            if (dto.getAddress() == null || isEmpty(dto.getAddress().getStreet()))
                throw new IllegalArgumentException("Dirección requerida para entrega a domicilio");
        }

        order o = new order();
        o.setBuyerEmail(dto.getBuyerEmail().trim().toLowerCase());
        o.setBuyerName(dto.getBuyerName().trim());
        o.setBuyerPhone(dto.getBuyerPhone());
        o.setStatus(OrderStatus.PENDING);
        o.setDeliveryMethod(dto.getDeliveryMethod());

        // Dirección si corresponde
        if (dto.getDeliveryMethod() == DeliveryMethod.DELIVERY) {
            ShippingAddress addr = new ShippingAddress();
            var a = dto.getAddress();
            addr.setStreet(n(a.getStreet()));
            addr.setNumber(n(a.getNumber()));
            addr.setApt(n(a.getApt()));
            addr.setReference(n(a.getReference()));
            addr.setCity(n(a.getCity()));
            addr.setProvince(n(a.getProvince()));
            addr.setPostalCode(n(a.getPostalCode()));
            addr.setLat(a.getLat());
            addr.setLng(a.getLng());
            o.setShippingAddress(addr);
        }

        BigDecimal delivery = (dto.getDeliveryCost() == null) ? BigDecimal.ZERO : dto.getDeliveryCost();
        o.setDeliveryCost(delivery);

        BigDecimal itemsTotal = BigDecimal.ZERO;

        for (CreateOrderItemDTO it : dto.getItems()) {
            product p = productRepo.lockById(it.getProductId());
            if (p == null || p.getIsActive() == null || !p.getIsActive())
                throw new IllegalArgumentException("Producto inválido: " + it.getProductId());

            int qty = it.getQuantity();
            if (qty <= 0)
                throw new IllegalArgumentException("Cantidad inválida para " + p.getName());

            int reserved = (p.getReserved() == null ? 0 : p.getReserved());
            int available = p.getStock() - reserved;
            if (available < qty)
                throw new IllegalArgumentException("Sin stock disponible para " + p.getName());

            // reservar
            p.setReserved(reserved + qty);
            productRepo.save(p);

            BigDecimal unit = p.getPrice();
            if (unit == null)
                throw new IllegalArgumentException("Producto sin precio: " + p.getName());

            BigDecimal lineGross = unit.multiply(BigDecimal.valueOf(qty));

            // === CÁLCULO DE DESCUENTO POR PRODUCTO ===
            int threshold = (p.getDiscountThreshold() != null)
                    ? p.getDiscountThreshold()
                    : DEFAULT_DISCOUNT_QTY;

            BigDecimal percent = (p.getDiscountPercent() != null)
                    ? p.getDiscountPercent()
                    : DEFAULT_DISCOUNT_PERCENT;

            BigDecimal discountAmount = BigDecimal.ZERO;

            if (qty >= threshold && percent.compareTo(BigDecimal.ZERO) > 0) {
                // lineGross * (percent / 100)
                discountAmount = lineGross
                        .multiply(percent)
                        .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
            }

            BigDecimal lineNet = lineGross.subtract(discountAmount);

            orderItem oi = new orderItem();
            oi.setOrder(o);
            oi.setProduct(p);
            oi.setQuantity(qty);
            oi.setUnitPrice(unit);          // precio original
            oi.setLineTotal(lineNet);       // total con descuento
            oi.setDiscountAmount(discountAmount);

            o.getItems().add(oi);
            itemsTotal = itemsTotal.add(lineNet); // sumás el neto, NO el bruto
        }

        o.setItemsTotal(itemsTotal);
        o.setGrandTotal(itemsTotal.add(delivery));

        o = orderRepo.save(o);

        try {
            mail.sendOrderConfirmationEmail(
                    o.getBuyerEmail(), o.getBuyerName(), o.getId(), o.getGrandTotal()
            );
        } catch (Exception ignored) {}

        return toDTO(o);
    }


    private boolean isEmpty(String s){ return s==null || s.trim().isEmpty(); }
    private String n(String s){ return s==null? null : s.trim(); }


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
        d.setDeliveryMethod(o.getDeliveryMethod());
        d.setDeliveryCost(o.getDeliveryCost());

        if (o.getShippingAddress()!=null) {
            ShippingAddressDTO ad = new ShippingAddressDTO();
            var a = o.getShippingAddress();
            ad.setStreet(a.getStreet());
            ad.setNumber(a.getNumber());
            ad.setApt(a.getApt());
            ad.setReference(a.getReference());
            ad.setCity(a.getCity());
            ad.setProvince(a.getProvince());
            ad.setPostalCode(a.getPostalCode());
            ad.setLat(a.getLat());
            ad.setLng(a.getLng());
            d.setAddress(ad);
        }
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
