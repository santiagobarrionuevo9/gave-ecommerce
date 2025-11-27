package org.example.gavebackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.example.gavebackend.repositories.appUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final appUserRepository repo;
    /**
     * Carga un usuario por su email para autenticación
     * @param email Email del usuario
     * @return Detalles del usuario para Spring Security
     * @throws UsernameNotFoundException Si no se encuentra el usuario
     */
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var u = repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No existe usuario"));
        return User.withUsername(u.getEmail())
                .password(u.getPasswordHash())
                .roles(u.getRole().name())
                .build();
    }
}
