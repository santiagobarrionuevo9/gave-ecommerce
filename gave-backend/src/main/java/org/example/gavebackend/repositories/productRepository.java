package org.example.gavebackend.repositories;

import jakarta.persistence.LockModeType;
import org.example.gavebackend.entities.product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.Optional;
@Repository
public interface productRepository extends JpaRepository<product, Long> {
    Optional<product> findBySlugAndIsActiveTrue(String slug);
    Optional<product> findBySku(String sku);
    boolean existsBySlug(String slug);
    boolean existsBySku(String sku);
    boolean existsBySlugAndIdNot(String slug, Long id);
    boolean existsBySkuAndIdNot(String sku, Long id);

    Page<product> findByIsActive(Boolean active, Pageable pageable);
    Page<product> findByTypeIdAndIsActive(Long typeId, Boolean active, Pageable pageable);
    Page<product> findByNameContainingIgnoreCaseAndIsActive(String q, Boolean active, Pageable pageable);
    Page<product> findByTypeIdAndNameContainingIgnoreCaseAndIsActive(Long typeId, String q, Boolean active, Pageable pageable);

    // NUEVO: busca por name, slug, sku, shortDesc, description y nombre del tipo
    @Query("""
      select p from product p
        join p.type t
      where (:active is null or p.isActive = :active)
        and (:typeId is null or t.id = :typeId)
        and (
             lower(p.name) like :like
          or lower(p.slug) like :like
          or lower(p.sku) like :like
          or lower(coalesce(p.shortDesc,'')) like :like
          or lower(coalesce(p.description,'')) like :like
          or lower(t.name) like :like
        )
    """)
    Page<product> searchAllFields(
            @Param("typeId") Long typeId,
            @Param("active") Boolean active,
            @Param("like") String like,
            Pageable pageable
    );

    // src/main/java/.../repositories/productRepository.java (agregar)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from product p where p.id = :id")
    product lockById(@Param("id") Long id);

}
