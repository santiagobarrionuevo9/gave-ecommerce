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

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

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
        t.setName(reqTrim(dto.getName(), "name"));
        t.setSlug(normalizeSlug(dto.getSlug(), "slug"));
        t.setDescription(nullSafeTrim(dto.getDescription()));
        t = typeRepo.save(t);
        dto.setId(t.getId());
        return dto;
    }

    @Override @Transactional
    public List<ProductTypeDTO> listTypes() {
        return typeRepo.findAll(Sort.by("name").ascending())
                .stream().map(this::toDTO).toList();
    }

    // ---- PRODUCTS ----
    @Override
    public ProductDTO create(ProductDTO dto) {
        // ---- Validaciones de request
        Long typeId = reqNotNull(dto.getTypeId(), "typeId");
        String name  = reqTrim(dto.getName(), "name");
        String slug  = normalizeSlug(dto.getSlug(), "slug");
        String sku   = reqTrim(dto.getSku(), "sku");
        BigDecimal price = reqNotNull(dto.getPrice(), "price");
        Integer stock    = reqNotNull(dto.getStock(), "stock");

        mustBePositiveOrZero(price, "price");
        mustBePositiveOrZero(stock, "stock");

        // ---- Reglas de unicidad
        if (productRepo.existsBySlug(slug))
            throw new DataIntegrityViolationException("Slug ya existe");
        if (productRepo.existsBySku(sku))
            throw new DataIntegrityViolationException("SKU ya existe");

        // ---- FK
        var type = typeRepo.findById(typeId)
                .orElseThrow(() -> new IllegalArgumentException("ProductType no encontrado"));

        // ---- Persistencia
        var p = new product();
        p.setType(type);
        p.setName(name);
        p.setSlug(slug);
        p.setShortDesc(nullSafeTrim(dto.getShortDesc()));
        p.setDescription(nullSafeTrim(dto.getDescription()));
        p.setIsActive(dto.getIsActive() == null ? true : dto.getIsActive());
        p.setSku(sku);
        p.setPrice(price);
        p.setStock(stock);

        p = productRepo.save(p);
        return toDTO(p);
    }

    @Override
    public ProductDTO update(Long id, ProductDTO dto) {
        var p = productRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product no encontrado"));

        // ---- Validaciones de request
        Long typeId = reqNotNull(dto.getTypeId(), "typeId");
        String name  = reqTrim(dto.getName(), "name");
        String slug  = normalizeSlug(dto.getSlug(), "slug");
        String sku   = reqTrim(dto.getSku(), "sku");
        BigDecimal price = reqNotNull(dto.getPrice(), "price");
        Integer stock    = reqNotNull(dto.getStock(), "stock");

        mustBePositiveOrZero(price, "price");
        mustBePositiveOrZero(stock, "stock");

        // ---- Unicidad (excluyendo el propio id)
        if (productRepo.existsBySlugAndIdNot(slug, id) && !Objects.equals(p.getSlug(), slug))
            throw new DataIntegrityViolationException("Slug ya existe");
        if (productRepo.existsBySkuAndIdNot(sku, id) && !Objects.equals(p.getSku(), sku))
            throw new DataIntegrityViolationException("SKU ya existe");

        // ---- FK
        var type = typeRepo.findById(typeId)
                .orElseThrow(() -> new IllegalArgumentException("ProductType no encontrado"));

        // ---- Set campos
        p.setType(type);
        p.setName(name);
        p.setSlug(slug);
        p.setShortDesc(nullSafeTrim(dto.getShortDesc()));
        p.setDescription(nullSafeTrim(dto.getDescription()));
        if (dto.getIsActive() != null) p.setIsActive(dto.getIsActive());
        p.setSku(sku);
        p.setPrice(price);
        p.setStock(stock);

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
        if (active == null) active = true;
        if (typeId != null && q != null && !q.isBlank())
            return productRepo.findByTypeIdAndNameContainingIgnoreCaseAndIsActive(typeId, q.trim(), active, pageable).map(this::toDTO);
        if (typeId != null)
            return productRepo.findByTypeIdAndIsActive(typeId, active, pageable).map(this::toDTO);
        if (q != null && !q.isBlank())
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
        Long productId = reqNotNull(dto.getProductId(), "productId");
        String url = reqTrim(dto.getUrl(), "url");
        if (dto.getSortOrder() != null && dto.getSortOrder() < 0)
            throw new IllegalArgumentException("sortOrder no puede ser negativo");

        var prod = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product no encontrado"));

        var img = new ProductImage();
        img.setProduct(prod);
        img.setUrl(url);
        img.setAltText(nullSafeTrim(dto.getAltText()));
        img.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        img = imageRepo.save(img);
        return toDTO(img);
    }

    @Override
    public void deleteImage(Long id) { imageRepo.deleteById(id); }

    @Override @Transactional
    public List<ProductImageDTO> listImagesByProduct(Long productId) {
        return imageRepo.findByProductIdOrderBySortOrderAsc(productId)
                .stream().map(this::toDTO).toList();
    }

    // ---- mappers ----
    private ProductTypeDTO toDTO(productType t){
        var dto = new ProductTypeDTO();
        dto.setId(t.getId());
        dto.setName(t.getName());
        dto.setSlug(t.getSlug());
        dto.setDescription(t.getDescription());
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

    // -------- helpers de validación --------

    private String nullSafeTrim(String s) {
        return (s == null) ? null : s.trim();
    }

    private String reqTrim(String s, String field) {
        if (s == null) throw new IllegalArgumentException(field + " es requerido");
        String v = s.trim();
        if (v.isEmpty()) throw new IllegalArgumentException(field + " no puede estar vacío");
        return v;
    }

    private <T> T reqNotNull(T v, String field) {
        if (v == null) throw new IllegalArgumentException(field + " es requerido");
        return v;
    }

    private void mustBePositiveOrZero(BigDecimal n, String field){
        if (n.signum() < 0) throw new IllegalArgumentException(field + " no puede ser negativo");
    }

    private void mustBePositiveOrZero(Integer n, String field){
        if (n < 0) throw new IllegalArgumentException(field + " no puede ser negativo");
    }

    /**
     * Normaliza slug (trim, toLowerCase) y valida formato básico.
     * Permite letras, números y guiones.
     */
    private String normalizeSlug(String slug, String field){
        String v = reqTrim(slug, field).toLowerCase();
        // reemplazos simples de tildes/comunes
        v = v.replace('á','a').replace('é','e').replace('í','i')
                .replace('ó','o').replace('ú','u').replace('ñ','n');
        // compactar espacios y dejar guiones
        v = v.replace(' ', '-');
        if (!v.matches("^[a-z0-9-]+$"))
            throw new IllegalArgumentException(field + " inválido (solo a-z, 0-9 y '-')");
        return v;
    }

}
