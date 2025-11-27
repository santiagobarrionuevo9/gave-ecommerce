package org.example.gavebackend.services.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ProductServiceImpl implements serviceproducts {

    private final productRepository productRepo;
    private final productTypeRepository typeRepo;
    private final productImageRepository imageRepo;
    private final CloudinaryService cloudinaryService;


    /**
     * Crea un nuevo tipo de producto (categoría) a partir del DTO recibido.
     * Valida nombre y slug, persiste la entidad y devuelve el DTO con el id generado.
     *
     * @param dto DTO con los datos del tipo de producto a crear.
     * @return DTO actualizado con el id del tipo creado.
     * @throws IllegalArgumentException si el nombre o el slug son inválidos.
     */
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

    /**
     * Lista todos los tipos de productos ordenados por nombre ascendente.
     *
     * @return Lista de DTOs de tipos de productos.
     */
    @Override
    @Transactional
    public List<ProductTypeDTO> listTypes() {
        return typeRepo.findAll(Sort.by("name").ascending())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Actualiza un tipo de producto existente con los datos del DTO recibido.
     * Valida nombre y slug, persiste los cambios y devuelve el DTO actualizado.
     *
     * @param dto DTO con los datos del tipo de producto a actualizar.
     * @return DTO actualizado.
     * @throws IllegalArgumentException si el tipo no existe o si el nombre o slug son inválidos.
     */
    @Override
    public ProductTypeDTO updateType(ProductTypeDTO dto) {
        var entity = typeRepo.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Tipo no encontrado"));

        entity.setName(dto.getName());
        entity.setSlug(dto.getSlug());
        entity.setDescription(dto.getDescription());
        typeRepo.save(entity);

        return toDTO(entity);
    }

    /**
     * Elimina un tipo de producto por su id.
     * Verifica que no existan productos asociados antes de eliminar.
     *
     * @param id Id del tipo de producto a eliminar.
     * @throws IllegalArgumentException si el tipo no existe o si hay productos asociados.
     */
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
            throw new IllegalArgumentException("No se puede eliminar por restricciones de integridad");
        }
    }


    /**
     * Crea un nuevo producto a partir del DTO recibido.
     * Valida los campos requeridos, verifica unicidad de slug y SKU,
     * persiste la entidad y devuelve el DTO con el id generado.
     *
     * @param dto DTO con los datos del producto a crear.
     * @return DTO actualizado con el id del producto creado.
     * @throws IllegalArgumentException si algún campo es inválido o si slug/SKU ya existen.
     */
    @Override
    public ProductDTO create(ProductDTO dto) {

        Long typeId= dto.getTypeId();
        String name= reqTrim(dto.getName(), "name");
        String slug= normalizeSlug(dto.getSlug(), "slug");
        String sku= reqTrim(dto.getSku(), "sku");
        BigDecimal price= dto.getPrice();
        Integer stock= dto.getStock();

        BigDecimal discountPercent  = dto.getDiscountPercent();
        Integer discountThreshold   = dto.getDiscountThreshold();

        if (productRepo.existsBySlug(slug))
            throw new DataIntegrityViolationException("Slug ya existe");
        if (productRepo.existsBySku(sku))
            throw new DataIntegrityViolationException("SKU ya existe");


        var type = typeRepo.findById(typeId)
                .orElseThrow(() -> new IllegalArgumentException("ProductType no encontrado"));

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
        p.setDiscountPercent(discountPercent == null ? BigDecimal.ZERO : discountPercent);
        p.setDiscountThreshold(discountThreshold == null ? 0 : discountThreshold);

        p = productRepo.save(p);
        return toDTO(p);
    }

    /**
     * Actualiza un producto existente con los datos del DTO recibido.
     * Valida los campos requeridos, verifica unicidad de slug y SKU,
     * persiste los cambios y devuelve el DTO actualizado.
     *
     * @param id  Id del producto a actualizar.
     * @param dto DTO con los datos del producto a actualizar.
     * @return DTO actualizado.
     * @throws IllegalArgumentException si el producto no existe o si algún campo es inválido.
     */
    @Override
    public ProductDTO update(Long id, ProductDTO dto) {
        var p = productRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product no encontrado"));


        Long typeId= dto.getTypeId();
        String name= reqTrim(dto.getName(), "name");
        String slug= normalizeSlug(dto.getSlug(), "slug");
        String sku= reqTrim(dto.getSku(), "sku");
        BigDecimal price= dto.getPrice();
        Integer stock= dto.getStock();

        BigDecimal discountPercent  = dto.getDiscountPercent();
        Integer discountThreshold   = dto.getDiscountThreshold();


        if (productRepo.existsBySlugAndIdNot(slug, id) && !Objects.equals(p.getSlug(), slug))
            throw new DataIntegrityViolationException("Slug ya existe");
        if (productRepo.existsBySkuAndIdNot(sku, id) && !Objects.equals(p.getSku(), sku))
            throw new DataIntegrityViolationException("SKU ya existe");


        var type = typeRepo.findById(typeId)
                .orElseThrow(() -> new IllegalArgumentException("ProductType no encontrado"));

        p.setType(type);
        p.setName(name);
        p.setSlug(slug);
        p.setShortDesc(nullSafeTrim(dto.getShortDesc()));
        p.setDescription(nullSafeTrim(dto.getDescription()));
        if (dto.getIsActive() != null) {
            p.setIsActive(dto.getIsActive());
        }
        p.setSku(sku);
        p.setPrice(price);
        p.setStock(stock);
        p.setDiscountPercent(discountPercent == null ? BigDecimal.ZERO : discountPercent);
        p.setDiscountThreshold(discountThreshold == null ? 0 : discountThreshold);

        return toDTO(productRepo.save(p));
    }

    /**
     * Obtiene un producto por su id.
     *
     * @param id Id del producto a buscar.
     * @return DTO del producto encontrado.
     * @throws IllegalArgumentException si el producto no existe.
     */
    @Override
    @Transactional
    public ProductDTO get(Long id) {
        return productRepo.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Product no encontrado"));
    }

    /**
     * Elimina un producto por su id.
     *
     * @param id Id del producto a eliminar.
     */
    @Override
    public void delete(Long id) {
        productRepo.deleteById(id);
    }

    /**
     * Busca productos según los criterios especificados.
     * Permite filtrar por texto de búsqueda, tipo de producto y estado activo/inactivo.
     * Devuelve una página de resultados con los DTOs correspondientes.
     *
     * @param q       Texto de búsqueda para nombre, descripción o SKU (opcional).
     * @param typeId  Id del tipo de producto para filtrar (opcional).
     * @param active  Estado activo/inactivo para filtrar (opcional, por defecto true).
     * @param pageable Información de paginación.
     * @return Página de DTOs de productos que cumplen los criterios.
     */
    @Override
    @Transactional
    public Page<ProductDTO> search(String q, Long typeId, Boolean active, Pageable pageable) {
        if (active == null) active = true;

        if (q != null && !q.isBlank()) {
            String like = "%" + q.trim().toLowerCase() + "%";
            return productRepo.searchAllFields(typeId, active, like, pageable)
                    .map(this::toDTO);
        }

        if (typeId != null) {
            return productRepo.findByTypeIdAndIsActive(typeId, active, pageable)
                    .map(this::toDTO);
        }

        return productRepo.findByIsActive(active, pageable)
                .map(this::toDTO);
    }

    /**
     * Obtiene un producto público por su slug.
     * Solo devuelve productos que estén activos.
     *
     * @param slug Slug del producto a buscar.
     * @return DTO del producto encontrado.
     * @throws IllegalArgumentException si el producto no existe o está inactivo.
     */
    @Override
    @Transactional
    public ProductDTO getPublicBySlug(String slug) {
        var p = productRepo.findBySlugAndIsActiveTrue(slug)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado o inactivo"));
        return toDTO(p);
    }

    /**
     * Actualiza el stock de un producto según la operación especificada en el DTO.
     * Permite establecer, incrementar o decrementar el stock.
     *
     * @param id  Id del producto cuyo stock se va a actualizar.
     * @param dto DTO con la operación y cantidad para el cambio de stock.
     * @return DTO del producto actualizado.
     * @throws EntityNotFoundException si el producto no existe.
     * @throws IllegalArgumentException si la operación es inválida.
     */
    @Override
    @Transactional
    public ProductDTO updateStock(Long id, StockChangeDTO dto) {
        product p = productRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));

        int current = p.getStock() == null ? 0 : p.getStock();
        int next;
        switch (dto.getOperation()) {
            case SET       -> next = dto.getAmount();
            case INCREMENT -> next = current + dto.getAmount();
            case DECREMENT -> next = Math.max(0, current - dto.getAmount());
            default        -> throw new IllegalArgumentException("Operación inválida");
        }

        p.setStock(next);
        return toDTO(p);
    }

    /**
     * Actualiza el stock de múltiples productos según las operaciones especificadas en la lista de DTOs.
     *
     * @param items Lista de DTOs con los ids de productos y las operaciones de cambio de stock.
     * @return Lista de DTOs de productos actualizados.
     * @throws EntityNotFoundException si algún producto no existe.
     * @throws IllegalArgumentException si alguna operación es inválida.
     */
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

    /**
     * Agrega una nueva imagen a un producto según los datos del DTO recibido.
     * Valida los campos requeridos, persiste la entidad y devuelve el DTO con el id generado.
     *
     * @param dto DTO con los datos de la imagen a agregar.
     * @return DTO actualizado con el id de la imagen creada.
     * @throws IllegalArgumentException si algún campo es inválido o si el producto no existe.
     */
    @Override
    public ProductImageDTO addImage(ProductImageDTO dto) {
        Long productId = dto.getProductId();
        String url     = reqTrim(dto.getUrl(), "url");

        if (dto.getSortOrder() != null && dto.getSortOrder() < 0) {
            throw new IllegalArgumentException("sortOrder no puede ser negativo");
        }

        var prod = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product no encontrado"));

        var img = new ProductImage();
        img.setProduct(prod);
        img.setUrl(url);
        img.setAltText(nullSafeTrim(dto.getAltText()));
        img.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        img.setCloudPublicId(dto.getCloudPublicId());

        img = imageRepo.save(img);
        return toDTO(img);
    }

    /**
     * Elimina una imagen de producto por su id.
     * Si la imagen está almacenada en Cloudinary, también la elimina de allí.
     *
     * @param id Id de la imagen a eliminar.
     * @throws IllegalArgumentException si la imagen no existe.
     */
    @Override
    public void deleteImage(Long id) {
        var img = imageRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Imagen no encontrada"));

        try {
            if (img.getCloudPublicId() != null && !img.getCloudPublicId().isBlank()) {
                cloudinaryService.delete(img.getCloudPublicId());
            }
        } catch (Exception ignored) {

        }

        imageRepo.deleteById(id);
    }
    /**
     * Lista todas las imágenes asociadas a un producto, ordenadas por sortOrder ascendente.
     *
     * @param productId Id del producto cuyas imágenes se van a listar.
     * @return Lista de DTOs de imágenes del producto.
     */
    @Override
    @Transactional
    public List<ProductImageDTO> listImagesByProduct(Long productId) {
        return imageRepo.findByProductIdOrderBySortOrderAsc(productId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**     * Convierte una entidad de pedido a DTO
     * @param t Entidad de tipo de producto
     * @return DTO de tipo de producto
     */
    private ProductTypeDTO toDTO(productType t) {
        var dto = new ProductTypeDTO();
        dto.setId(t.getId());
        dto.setName(t.getName());
        dto.setSlug(t.getSlug());
        dto.setDescription(t.getDescription());
        return dto;
    }

    /**     * Convierte una entidad de pedido a DTO
     * @param p Entidad de producto
     * @return DTO de producto
     */
    private ProductDTO toDTO(product p) {
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
        dto.setDiscountThreshold(p.getDiscountThreshold());
        dto.setDiscountPercent(p.getDiscountPercent());
        dto.setCreatedAt(p.getCreatedAt());
        return dto;
    }

    /**     * Convierte una entidad de pedido a DTO
     * @param img Entidad de imagen de producto
     * @return DTO de tipo de imagen de producto
     */
    private ProductImageDTO toDTO(ProductImage img) {
        var dto = new ProductImageDTO();
        dto.setId(img.getId());
        dto.setProductId(img.getProduct().getId());
        dto.setUrl(img.getUrl());
        dto.setAltText(img.getAltText());
        dto.setSortOrder(img.getSortOrder());
        dto.setCloudPublicId(img.getCloudPublicId());
        return dto;
    }

    private String nullSafeTrim(String s) {
        return (s == null) ? null : s.trim();
    }

    private String reqTrim(String s, String field) {
        if (s == null) {
            throw new IllegalArgumentException(field + " es requerido");
        }
        String v = s.trim();
        if (v.isEmpty()) {
            throw new IllegalArgumentException(field + " no puede estar vacío");
        }
        return v;
    }

    private String normalizeSlug(String slug, String field) {
        String v = reqTrim(slug, field).toLowerCase();
        v = v.replace('á', 'a').replace('é', 'e').replace('í', 'i')
                .replace('ó', 'o').replace('ú', 'u').replace('ñ', 'n');
        v = v.replace(' ', '-');
        if (!v.matches("^[a-z0-9-]+$")) {
            throw new IllegalArgumentException(field + " inválido (solo a-z, 0-9 y '-')");
        }
        return v;
    }

}
