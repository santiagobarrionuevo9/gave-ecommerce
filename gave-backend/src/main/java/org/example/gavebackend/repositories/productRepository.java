package org.example.gavebackend.repositories;

import org.example.gavebackend.entities.product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;
@Repository
public interface productRepository extends JpaRepository<product, Long> {
    Optional<product> findBySlugAndIsActiveTrue(String slug);
    Optional<product> findBySku(String sku);
    boolean existsBySku(String sku);
    boolean existsBySlug(String slug);

    // para validar unicidad al actualizar (excluyendo el propio id)
    boolean existsBySkuAndIdNot(String sku, Long id);
    boolean existsBySlugAndIdNot(String slug, Long id);


    Page<product> findByIsActive(Boolean active, Pageable pageable);
    Page<product> findByNameContainingIgnoreCaseAndIsActive(String q, Boolean active, Pageable pageable);
    Page<product> findByTypeIdAndIsActive(Long typeId, Boolean active, Pageable pageable);
    Page<product> findByTypeIdAndNameContainingIgnoreCaseAndIsActive(Long typeId, String q, Boolean active, Pageable pageable);
}
