package org.example.gavebackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_image")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="product_id", nullable=false)
    private product product;

    @Column(nullable=false, length=255)
    private String url;

    @Column(name="alt_text", length=160)
    private String altText;

    @Column(name="sort_order")
    private Integer sortOrder = 0;
}