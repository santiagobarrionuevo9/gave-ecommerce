package org.example.gavebackend.controllers;


import jakarta.validation.Valid;
import org.example.gavebackend.dtos.*;

import org.example.gavebackend.services.serviceproducts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class productcontroller {
    @Autowired
    private serviceproducts service;

    // TYPES
    @PostMapping("/types")
    public ProductTypeDTO createType(@Valid @RequestBody ProductTypeDTO dto){ return service.createType(dto); }

    @GetMapping("/types")
    public List<ProductTypeDTO> listTypes(){ return service.listTypes(); }

    // productcontroller.java
    @PutMapping("/types/{id}")
    public ProductTypeDTO updateType(@PathVariable Long id, @Valid @RequestBody ProductTypeDTO dto){
        dto.setId(id);
        return service.updateType(dto);
    }

    @DeleteMapping("/types/{id}")
    public void deleteType(@PathVariable Long id){
        service.deleteType(id);
    }


    // PRODUCTS
    @PostMapping
    public ProductDTO create(@Valid @RequestBody ProductDTO dto){ return service.create(dto); }

    // src/main/java/org/example/gavebackend/controllers/productcontroller.java
    @PostMapping("/{id}/stock")
    public ProductDTO updateStock(@PathVariable Long id, @Valid @RequestBody StockChangeDTO dto) {
        return service.updateStock(id, dto);
    }

    @PostMapping("/stock/bulk")
    public List<ProductDTO> bulkUpdateStock(@Valid @RequestBody List<StockChangeDTOItem> items) {
        // items = [{ productId, operation, amount, reason }]
        return service.bulkUpdateStock(items);
    }

    @GetMapping
    public Page<ProductDTO> search(
            @RequestParam(required=false) String q,
            @RequestParam(required=false) Long typeId,
            @RequestParam(required=false) Boolean active,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="10") int size,
            @RequestParam(defaultValue="name,asc") String sort
    ){
        Sort s = parseSort(sort);
        return service.search(q, typeId, active, PageRequest.of(page, size, s));
    }

    @GetMapping("/{id}")
    public ProductDTO get(@PathVariable Long id){ return service.get(id); }

    @PutMapping("/{id}")
    public ProductDTO update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto){
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){ service.delete(id); }

    // IMAGES
    @PostMapping("/{productId}/images")
    public ProductImageDTO addImageToProduct(@PathVariable Long productId, @Valid @RequestBody ProductImageDTO dto){
        dto.setProductId(productId);
        return service.addImage(dto);
    }

    @GetMapping("/{productId}/images")
    public List<ProductImageDTO> listImages(@PathVariable Long productId){
        return service.listImagesByProduct(productId);
    }

    @DeleteMapping("/images/{imageId}")
    public void deleteImage(@PathVariable Long imageId){
        service.deleteImage(imageId);
    }

    private Sort parseSort(String sortParam){
        String[] p = sortParam.split(",");
        return (p.length==2) ? Sort.by(Sort.Direction.fromString(p[1]), p[0]) : Sort.by("name").ascending();
    }
}

