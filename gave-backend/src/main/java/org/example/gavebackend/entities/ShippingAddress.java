package org.example.gavebackend.entities;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class ShippingAddress {
    private String street;       // ej: "De los Toscano"
    private String number;       // "6048" o "S/N"
    private String apt;          // "Piso 3 Dpto B" (opcional)
    private String reference;    // "Puerta negra, timbre roto" (opcional)
    private String city;         // "Córdoba"
    private String province;     // "Córdoba"
    private String postalCode;   // "5021"
    private Double lat;          // opcional
    private Double lng;          // opcional
}