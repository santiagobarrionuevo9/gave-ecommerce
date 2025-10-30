package org.example.gavebackend.dtos;


import lombok.Data;

@Data
public class ShippingAddressDTO {
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
