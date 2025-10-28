package org.example.gavebackend.entities.enums;

public enum OrderStatus {
    PENDING,        // creado por cliente, en espera de confirmación del vendedor
    ACCEPTED,       // aceptado por vendedor
    PREPARING,      // preparando
    OUT_FOR_DELIVERY, // en camino (opcional)
    DELIVERED,      // entregado -> acá se descuenta stock
    CANCELED        // cancelado por vendedor/cliente -> libera reserva
}