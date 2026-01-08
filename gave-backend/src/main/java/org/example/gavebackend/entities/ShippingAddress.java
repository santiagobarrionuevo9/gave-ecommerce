package org.example.gavebackend.entities;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class ShippingAddress {
    private String street;
    private String number;
    private String apt;
    private String reference;
    private String city;
    private String province;
    private String postalCode;
    private Double lat;
    private Double lng;
}