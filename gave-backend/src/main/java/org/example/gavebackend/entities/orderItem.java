package org.example.gavebackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name="order_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class orderItem {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="order_id")
    private order order;

    @ManyToOne(optional=false) @JoinColumn(name="product_id")
    private product product;

    @Column(nullable=false) private Integer quantity;
    @Column(nullable=false) private BigDecimal unitPrice; // precio al momento del pedido
    @Column(nullable=false) private BigDecimal lineTotal; // unitPrice * quantity
}
