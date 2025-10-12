package org.example.gavebackend.entities;


import jakarta.persistence.*;
import lombok.*;


import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "product")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name="type_id")
    private productType type;

    @Column(nullable=false, length=180)
    private String name;

    @Column(unique=true, length=200)
    private String slug;

    @Column(length=300, name="short_desc")
    private String shortDesc;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name="is_active")
    private Boolean isActive = true;

    // Campos unificados (antes en variant)
    @Column(unique=true, length=64)
    private String sku;                 // si querés, podés dejarlo nullable

    @Column(precision=12, scale=2)
    private BigDecimal price;           // si querés, podés dejarlo nullable

    @Column(nullable=false)
    private Integer stock = 0;

    @Column(name="created_at", updatable=false)
    private Instant createdAt;

    @Column(name="updated_at")
    private Instant updatedAt;

    @PrePersist void prePersist(){ createdAt = Instant.now(); }
    @PreUpdate  void preUpdate(){  updatedAt = Instant.now();  }
}
