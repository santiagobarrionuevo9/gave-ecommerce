package org.example.gavebackend.entities;

import jakarta.persistence.*;
import lombok.*;


import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "product_variant")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class productVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="product_id")
    private org.example.gavebackend.entities.product product;

    @Column(nullable=false, unique=true, length=64) private String sku;
    @Column(length=180) private String title;
    @Column(nullable=false, precision=12, scale=2) private BigDecimal price;
    @Column(nullable=false) private Integer stock = 0;

    private Boolean isActive = true;
    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist void prePersist(){ createdAt = Instant.now(); }
    @PreUpdate  void preUpdate(){  updatedAt = Instant.now();  }
}

