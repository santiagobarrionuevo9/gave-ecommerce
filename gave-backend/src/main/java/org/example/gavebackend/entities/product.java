package org.example.gavebackend.entities;


import jakarta.persistence.*;
import lombok.*;
import org.example.gavebackend.entities.enums.StockLevel;


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

    /** NUEVO: descuento por cantidad */
    @Column(name = "discount_threshold")
    private Integer discountThreshold;       // ej: 10

    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent;      // ej: 10.00 -> 10%

    @Column(nullable=false)
    private Integer stock = 0;

    @Column(name = "stock_low_threshold")
    private Integer stockLowThreshold = 5;     // 🔴 peligro

    @Column(name = "stock_medium_threshold")
    private Integer stockMediumThreshold = 15; // 🟡 moderado


    @Column(name="created_at", updatable=false)
    private Instant createdAt;

    @Column(name="updated_at")
    private Instant updatedAt;
    /** NUEVO: unidades reservadas (pendientes en pedidos no entregados) */
    @Column(nullable = false)
    private Integer reserved = 0;

    @PrePersist void prePersist(){ createdAt = Instant.now(); }
    @PreUpdate  void preUpdate(){  updatedAt = Instant.now();  }

    @Transient
    public int getAvailableStock() {
        int s = stock == null ? 0 : stock;
        int r = reserved == null ? 0 : reserved;
        return Math.max(0, s - r);
    }

    @Transient
    public StockLevel getStockLevel() {
        int available = getAvailableStock();

        if (stockLowThreshold != null && available <= stockLowThreshold) {
            return StockLevel.DANGER;
        }
        if (stockMediumThreshold != null && available <= stockMediumThreshold) {
            return StockLevel.MODERATE;
        }
        return StockLevel.OK;
    }

}


