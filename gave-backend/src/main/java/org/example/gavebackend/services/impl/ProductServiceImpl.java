package org.example.gavebackend.services.impl;

import jakarta.transaction.Transactional;
import org.example.gavebackend.dtos.ProductDTO;
import org.example.gavebackend.dtos.ProductImageDTO;
import org.example.gavebackend.dtos.ProductTypeDTO;
import org.example.gavebackend.entities.ProductImage;
import org.example.gavebackend.entities.product;
import org.example.gavebackend.entities.productType;
import org.example.gavebackend.repositories.productImageRepository;
import org.example.gavebackend.repositories.productRepository;
import org.example.gavebackend.repositories.productTypeRepository;
import org.example.gavebackend.services.serviceproducts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements serviceproducts {
    @Autowired
    private  productRepository productRepo;
    @Autowired
    private  productTypeRepository typeRepo;
    @Autowired
    private productImageRepository imageRepo;;

    // ---- TYPES ----
    @Override
    public ProductTypeDTO createType(ProductTypeDTO dto) {
        var t = new productType();
        t.setName(dto.getName());
        t.setSlug(dto.getSlug());
        t.setDescription(dto.getDescription());
        t = typeRepo.save(t);
        dto.setId(t.getId());
        return dto;
    }

    @Override @Transactional
    public List<ProductTypeDTO> listTypes() {
        return typeRepo.findAll(Sort.by("name").ascending()).stream().map(this::toDTO).toList();
    }

    // ---- PRODUCTS ----
    @Override
    public ProductDTO create(ProductDTO dto) {
        if (productRepo.existsBySlug(dto.getSlug()))
            throw new DataIntegrityViolationException("Slug ya existe");
        if (productRepo.existsBySku(dto.getSku()))
            throw new DataIntegrityViolationException("SKU ya existe");

        var type = typeRepo.findById(dto.getTypeId())
                .orElseThrow(() -> new IllegalArgumentException("ProductType no encontrado"));

        var p = new product();
        p.setType(type);
        p.setName(dto.getName());
        p.setSlug(dto.getSlug());
        p.setShortDesc(dto.getShortDesc());
        p.setDescription(dto.getDescription());
        p.setIsActive(dto.getIsActive()==null?true:dto.getIsActive());
        p.setSku(dto.getSku());
        p.setPrice(dto.getPrice());
        p.setStock(dto.getStock());

        p = productRepo.save(p);
        return toDTO(p);
    }

    @Override
    public ProductDTO update(Long id, ProductDTO dto) {
        var p = productRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Product no encontrado"));

        if (!p.getSlug().equals(dto.getSlug()) && productRepo.existsBySlug(dto.getSlug()))
            throw new DataIntegrityViolationException("Slug ya existe");
        if (!p.getSku().equals(dto.getSku()) && productRepo.existsBySku(dto.getSku()))
            throw new DataIntegrityViolationException("SKU ya existe");

        var type = typeRepo.findById(dto.getTypeId())
                .orElseThrow(() -> new IllegalArgumentException("ProductType no encontrado"));

        p.setType(type);
        p.setName(dto.getName());
        p.setSlug(dto.getSlug());
        p.setShortDesc(dto.getShortDesc());
        p.setDescription(dto.getDescription());
        if (dto.getIsActive()!=null) p.setIsActive(dto.getIsActive());
        p.setSku(dto.getSku());
        p.setPrice(dto.getPrice());
        p.setStock(dto.getStock());

        return toDTO(productRepo.save(p));
    }

    @Override @Transactional
    public ProductDTO get(Long id) {
        return productRepo.findById(id).map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Product no encontrado"));
    }

    @Override
    public void delete(Long id) {
        productRepo.deleteById(id);
    }

    @Override @Transactional
    public Page<ProductDTO> search(String q, Long typeId, Boolean active, Pageable pageable) {
        if (active==null) active=true;
        if (typeId!=null && q!=null && !q.isBlank())
            return productRepo.findByTypeIdAndNameContainingIgnoreCaseAndIsActive(typeId, q.trim(), active, pageable).map(this::toDTO);
        if (typeId!=null)
            return productRepo.findByTypeIdAndIsActive(typeId, active, pageable).map(this::toDTO);
        if (q!=null && !q.isBlank())
            return productRepo.findByNameContainingIgnoreCaseAndIsActive(q.trim(), active, pageable).map(this::toDTO);
        return productRepo.findByIsActive(active, pageable).map(this::toDTO);
    }

    @Override @Transactional
    public ProductDTO getPublicBySlug(String slug) {
        var p = productRepo.findBySlugAndIsActiveTrue(slug)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado o inactivo"));
        return toDTO(p);
    }

    // ---- IMAGES ----
    @Override
    public ProductImageDTO addImage(ProductImageDTO dto) {
        var prod = productRepo.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product no encontrado"));

        var img = new ProductImage();
        img.setProduct(prod);
        img.setUrl(dto.getUrl());
        img.setAltText(dto.getAltText());
        img.setSortOrder(dto.getSortOrder()==null?0:dto.getSortOrder());
        img = imageRepo.save(img);
        return toDTO(img);
    }

    @Override
    public void deleteImage(Long id) { imageRepo.deleteById(id); }

    @Override @Transactional
    public List<ProductImageDTO> listImagesByProduct(Long productId) {
        return imageRepo.findByProductIdOrderBySortOrderAsc(productId).stream().map(this::toDTO).toList();
    }

    // ---- mappers ----
    private ProductTypeDTO toDTO(productType t){
        var dto = new ProductTypeDTO();
        dto.setId(t.getId()); dto.setName(t.getName()); dto.setSlug(t.getSlug()); dto.setDescription(t.getDescription());
        return dto;
    }

    private ProductDTO toDTO(product p){
        var dto = new ProductDTO();
        dto.setId(p.getId());
        dto.setTypeId(p.getType().getId());
        dto.setName(p.getName());
        dto.setSlug(p.getSlug());
        dto.setShortDesc(p.getShortDesc());
        dto.setDescription(p.getDescription());
        dto.setIsActive(p.getIsActive());
        dto.setSku(p.getSku());
        dto.setPrice(p.getPrice());
        dto.setStock(p.getStock());
        return dto;
    }

    private ProductImageDTO toDTO(ProductImage img){
        var dto = new ProductImageDTO();
        dto.setId(img.getId());
        dto.setProductId(img.getProduct().getId());
        dto.setUrl(img.getUrl());
        dto.setAltText(img.getAltText());
        dto.setSortOrder(img.getSortOrder());
        return dto;
    }
}
