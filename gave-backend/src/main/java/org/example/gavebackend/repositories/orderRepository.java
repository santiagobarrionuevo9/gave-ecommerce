package org.example.gavebackend.repositories;

import org.example.gavebackend.entities.enums.OrderStatus;
import org.example.gavebackend.entities.order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface orderRepository extends JpaRepository<order, Long> {
    Page<order> findByBuyerEmailOrderByCreatedAtDesc(String email, Pageable pg);
    Page<order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pg);
}
