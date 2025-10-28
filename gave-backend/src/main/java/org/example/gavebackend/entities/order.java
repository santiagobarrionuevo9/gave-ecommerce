package org.example.gavebackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.gavebackend.entities.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // para rastrear al comprador (por email es suficiente si no usás usuarios)
    @Column(nullable=false) private String buyerEmail;
    @Column(nullable=false) private String buyerName;
    @Column(nullable=true)  private String buyerPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable=false) private BigDecimal itemsTotal; // suma de (price * qty)
    @Column(nullable=false) private BigDecimal grandTotal; // por si sumas delivery, etc.

    @Column(nullable=false) private Instant createdAt;
    @Column(nullable=false) private Instant updatedAt;

    @OneToMany(mappedBy="order", cascade=CascadeType.ALL, orphanRemoval = true, fetch=FetchType.LAZY)
    private List<orderItem> items = new ArrayList<>();

    @PrePersist void prePersist(){
        Instant now = Instant.now();
        createdAt = now; updatedAt = now;
    }
    @PreUpdate void preUpdate(){ updatedAt = Instant.now(); }

}
