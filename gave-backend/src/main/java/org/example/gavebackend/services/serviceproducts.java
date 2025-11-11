package org.example.gavebackend.services;

import org.example.gavebackend.dtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface serviceproducts {
    // Types
    ProductTypeDTO createType(ProductTypeDTO dto);
    List<ProductTypeDTO> listTypes();

    // Products
    ProductDTO create(ProductDTO dto);
    ProductDTO update(Long id, ProductDTO dto);
    ProductDTO get(Long id);
    void delete(Long id);
    Page<ProductDTO> search(String q, Long typeId, Boolean active, Pageable pageable);
    ProductDTO getPublicBySlug(String slug);

    // Images
    ProductImageDTO addImage(ProductImageDTO dto);
    void deleteImage(Long id);
    List<ProductImageDTO> listImagesByProduct(Long productId);
    // src/main/java/org/example/gavebackend/services/serviceproducts.java
    public ProductDTO updateStock(Long id, StockChangeDTO dto);
    public List<ProductDTO> bulkUpdateStock(List<StockChangeDTOItem> items);

    void deleteType(Long id);

    public ProductTypeDTO updateType(ProductTypeDTO dto);


}
