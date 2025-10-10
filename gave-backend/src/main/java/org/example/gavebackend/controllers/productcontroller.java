package org.example.gavebackend.controllers;


import org.example.gavebackend.entities.product;
import org.example.gavebackend.repositories.productRepository;
import org.example.gavebackend.repositories.productTypeRepository;
import org.example.gavebackend.repositories.productVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class productcontroller {
    @Autowired
    private productRepository productRepo;

    @Autowired
    private productVariantRepository variantRepo;

    @Autowired
    private productTypeRepository typeRepo;

    @GetMapping
    public Page<product> list(
            @RequestParam(required=false) Long typeId,
            @PageableDefault(size=12, sort="createdAt", direction= Sort.Direction.DESC) Pageable pageable) {
        return (typeId == null)
                ? productRepo.findAll(pageable)
                : productRepo.findByType_IdAndIsActiveTrue(typeId, pageable);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<Map<String,Object>> bySlug(@PathVariable String slug){
        return productRepo.findBySlug(slug)
                .map(p -> Map.of("product", p, "variants", variantRepo.findByProduct_Id(p.getId())))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

