package org.example.gavebackend.entities;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "product_type")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class productType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, length=120) private String name;
    @Column(unique=true, length=160) private String slug;
    @Column(columnDefinition = "text") private String description;
}

