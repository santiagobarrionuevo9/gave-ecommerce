package org.example.gavebackend.services.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.example.gavebackend.dtos.*;
import org.example.gavebackend.entities.ProductImage;
import org.example.gavebackend.entities.product;
import org.example.gavebackend.entities.productType;
import org.example.gavebackend.repositories.productImageRepository;
import org.example.gavebackend.repositories.productRepository;
import org.example.gavebackend.repositories.productTypeRepository;
import org.example.gavebackend.services.serviceproducts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ProductServiceImpl implements serviceproducts {
    @Autowired
    private  productRepository productRepo;
    @Autowired
    private  productTypeRepository typeRepo;
    @Autowired
    private productImageRepository imageRepo;
    @Autowired
    private CloudinaryService cloudinaryService;

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

    // src/main/java/org/example/gavebackend/services/impl/serviceproductsImpl.java
    @Override
    @Transactional
    public ProductDTO updateStock(Long id, StockChangeDTO dto) {
        product p = productRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        int current = p.getStock() == null ? 0 : p.getStock();
        int next;
        switch (dto.getOperation()) {
            case SET -> next = dto.getAmount();
            case INCREMENT -> next = current + dto.getAmount();
            case DECREMENT -> next = Math.max(0, current - dto.getAmount());
            default -> throw new IllegalArgumentException("Operación inválida");
        }
        p.setStock(next);
        // opcional: auditar reason, updatedAt se setea en @PreUpdate
        return toDTO(p);
    }

    @Override
    @Transactional
    public List<ProductDTO> bulkUpdateStock(List<StockChangeDTOItem> items) {
        List<ProductDTO> result = new ArrayList<>();
        for (var it : items) {
            var dto = new StockChangeDTO();
            dto.setOperation(it.getOperation());
            dto.setAmount(it.getAmount());
            dto.setReason(it.getReason());
            result.add(updateStock(it.getProductId(), dto));
        }
        return result;
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

    @Override
    @Transactional
    public Page<ProductDTO> search(String q, Long typeId, Boolean active, Pageable pageable) {
        if (active == null) active = true;

        if (q != null && !q.isBlank()) {
            String like = "%" + q.trim().toLowerCase() + "%";
            return productRepo.searchAllFields(typeId, active, like, pageable).map(this::toDTO);
        }

        if (typeId != null)
            return productRepo.findByTypeIdAndIsActive(typeId, active, pageable).map(this::toDTO);

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
        img.setCloudPublicId(dto.getCloudPublicId()); // NUEVO
        img = imageRepo.save(img);
        return toDTO(img);
    }

    @Override
    public void deleteImage(Long id) {
        var img = imageRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Imagen no encontrada"));
        try {
            if (img.getCloudPublicId() != null && !img.getCloudPublicId().isBlank()) {
                cloudinaryService.delete(img.getCloudPublicId());
            }
        } catch (Exception ignored) {
            // podés loguear el error, pero no rompas la UX por fallar el delete remoto
        }
        imageRepo.deleteById(id);
    }

    @Override @Transactional
    public List<ProductImageDTO> listImagesByProduct(Long productId) {
        return imageRepo.findByProductIdOrderBySortOrderAsc(productId)
                .stream().map(this::toDTO).toList();
    }
    @Override
    public ProductTypeDTO updateType(ProductTypeDTO dto){
        var entity = typeRepo.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Tipo no encontrado"));

        entity.setName(dto.getName());
        entity.setSlug(dto.getSlug());
        entity.setDescription(dto.getDescription());
        typeRepo.save(entity);

        return toDTO(entity);
    }
    @Override
    public void deleteType(Long id) {
        var entity = typeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tipo no encontrado"));

        if (productRepo.existsByTypeId(id)) {
            throw new IllegalArgumentException("No se puede eliminar: hay productos asociados a esta categoría");
        }

        try {
            typeRepo.delete(entity);
        } catch (DataIntegrityViolationException ex) {
            // Podés crear otra excepción @ResponseStatus(HttpStatus.CONFLICT) si querés
            throw new IllegalArgumentException("No se puede eliminar por restricciones de integridad");
        }
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
        dto.setCreatedAt(p.getCreatedAt()); // 👈
        return dto;
    }

    private ProductImageDTO toDTO(ProductImage img){
        var dto = new ProductImageDTO();
        dto.setId(img.getId());
        dto.setProductId(img.getProduct().getId());
        dto.setUrl(img.getUrl());
        dto.setAltText(img.getAltText());
        dto.setSortOrder(img.getSortOrder());
        dto.setCloudPublicId(img.getCloudPublicId()); // NUEVO
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
