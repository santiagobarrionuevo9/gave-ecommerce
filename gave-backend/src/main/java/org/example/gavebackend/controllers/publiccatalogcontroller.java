package org.example.gavebackend.controllers;

import lombok.RequiredArgsConstructor;
import org.example.gavebackend.dtos.ProductDTO;
import org.example.gavebackend.dtos.ProductImageDTO;
import org.example.gavebackend.services.serviceproducts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class publiccatalogcontroller {

    @Autowired
    private serviceproducts service;

    @GetMapping("/products/{slug}")
    public ProductDTO getBySlug(@PathVariable String slug){
        return service.getPublicBySlug(slug);
    }

    @GetMapping("/products/{slug}/images")
    public List<ProductImageDTO> imagesByProductSlug(@PathVariable String slug){
        ProductDTO p = service.getPublicBySlug(slug);
        return service.listImagesByProduct(p.getId());
    }
}
