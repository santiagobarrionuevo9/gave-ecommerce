package org.example.gavebackend.services;

import org.example.gavebackend.dtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface serviceproducts {

    ProductTypeDTO createType(ProductTypeDTO dto);
    List<ProductTypeDTO> listTypes();
    ProductDTO create(ProductDTO dto);
    ProductDTO update(Long id, ProductDTO dto);
    ProductDTO get(Long id);
    void delete(Long id);
    Page<ProductDTO> search(String q, Long typeId, Boolean active, Pageable pageable);
    ProductDTO getPublicBySlug(String slug);
    ProductImageDTO addImage(ProductImageDTO dto);
    void deleteImage(Long id);
    List<ProductImageDTO> listImagesByProduct(Long productId);
    public ProductDTO updateStock(Long id, StockChangeDTO dto);
    public List<ProductDTO> bulkUpdateStock(List<StockChangeDTOItem> items);
    void deleteType(Long id);
    public ProductTypeDTO updateType(ProductTypeDTO dto);

    public List<ProductDTO> listLowStock();

    BulkDiscountByNameResponse bulkUpdateDiscountByName(BulkDiscountByNameRequest req);



}
