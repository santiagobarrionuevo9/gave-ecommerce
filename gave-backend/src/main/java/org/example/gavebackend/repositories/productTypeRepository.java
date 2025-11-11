package org.example.gavebackend.repositories;

import org.example.gavebackend.entities.productType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;
@Repository
public interface productTypeRepository extends JpaRepository<productType, Long> {
    boolean existsBySlug(String slug);
}
