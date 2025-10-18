package org.example.gavebackend.repositories;

import org.example.gavebackend.entities.appUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface appUserRepository extends JpaRepository<appUser, Long> {
    Optional<appUser> findByEmail(String email);
    boolean existsByEmail(String email);
}
