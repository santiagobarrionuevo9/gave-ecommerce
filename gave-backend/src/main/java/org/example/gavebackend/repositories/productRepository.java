package org.example.gavebackend.repositories;

import jakarta.persistence.LockModeType;
import org.example.gavebackend.entities.product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
@Repository
public interface productRepository extends JpaRepository<product, Long> {
    Optional<product> findBySlugAndIsActiveTrue(String slug);
    boolean existsBySlug(String slug);
    boolean existsBySku(String sku);
    boolean existsBySlugAndIdNot(String slug, Long id);
    boolean existsBySkuAndIdNot(String sku, Long id);
    Page<product> findByIsActive(Boolean active, Pageable pageable);
    Page<product> findByTypeIdAndIsActive(Long typeId, Boolean active, Pageable pageable);
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
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from product p where p.id = :id")
    product lockById(@Param("id") Long id);
    boolean existsByTypeId(Long typeId);

    // 👇 NUEVO: productos con stock disponible por debajo de los umbrales

    // Solo críticos (rojo)
    @Query("""
           SELECT p
           FROM product p
           WHERE p.isActive = true
             AND (p.stock - p.reserved) <= p.stockLowThreshold
           ORDER BY (p.stock - p.reserved) ASC
           """)
    List<product> findAllWithDangerStock();

    // Críticos + moderados (rojo o amarillo)
    @Query("""
           SELECT p
           FROM product p
           WHERE p.isActive = true
             AND (
                  (p.stock - p.reserved) <= p.stockLowThreshold
               OR (p.stock - p.reserved) <= p.stockMediumThreshold
             )
           ORDER BY (p.stock - p.reserved) ASC
           """)
    List<product> findAllWithLowOrModerateStock();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
   update product p
      set p.discountThreshold = coalesce(:discountThreshold, p.discountThreshold),
          p.discountPercent   = coalesce(:discountPercent, p.discountPercent)
    where lower(p.name) like concat('%', lower(:keyword), '%')
      and (:activeOnly = false or p.isActive = true)
""")
    int bulkUpdateDiscountByNameKeyword(
            @Param("keyword") String keyword,
            @Param("discountThreshold") Integer discountThreshold,
            @Param("discountPercent") BigDecimal discountPercent,
            @Param("activeOnly") boolean activeOnly
    );
}
