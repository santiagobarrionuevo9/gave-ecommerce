package org.example.gavebackend.entities;

import jakarta.persistence.*;
import lombok.*;
import org.example.gavebackend.entities.enums.Rol;

import java.time.Instant;

@Entity
@Table(name="app_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class appUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=160)
    private String email;

    @Column(nullable=false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private Rol role;

    private String fullName;
    private Instant createdAt;

    @PrePersist void pre(){ createdAt = Instant.now(); }
}
