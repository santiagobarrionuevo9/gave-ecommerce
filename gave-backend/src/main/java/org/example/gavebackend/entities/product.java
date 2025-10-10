package org.example.gavebackend.entities;


import jakarta.persistence.*;
import lombok.*;


import java.time.Instant;

@Entity
@Table(name = "product")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="type_id")
    private productType type;

    @Column(nullable=false, length=180) private String name;
    @Column(unique=true, length=200) private String slug;
    @Column(length=300) private String shortDesc;
    @Column(columnDefinition = "text") private String description;
    private Boolean isActive = true;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist void prePersist(){ createdAt = Instant.now(); }
    @PreUpdate  void preUpdate(){  updatedAt = Instant.now();  }

}

