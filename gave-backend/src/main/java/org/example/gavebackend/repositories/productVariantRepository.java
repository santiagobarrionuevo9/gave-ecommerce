package org.example.gavebackend.repositories;

import org.example.gavebackend.entities.productVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;
@Repository
public interface productVariantRepository extends JpaRepository<productVariant, Long> {
    Optional<productVariant> findBySku(String sku);
    List<productVariant> findByProduct_Id(Long productId);
}