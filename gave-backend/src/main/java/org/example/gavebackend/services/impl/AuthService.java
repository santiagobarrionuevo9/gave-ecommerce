package org.example.gavebackend.services.impl;

import org.example.gavebackend.dtos.AuthResponse;
import org.example.gavebackend.dtos.LoginRequest;
import org.example.gavebackend.dtos.RegisterRequest;
import org.example.gavebackend.entities.enums.Rol;
import org.example.gavebackend.entities.appUser;
import org.example.gavebackend.repositories.appUserRepository;
import org.example.gavebackend.services.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {
    @Autowired
    private  appUserRepository repo;
    @Autowired
    private  PasswordEncoder encoder;
    @Autowired
    private  JwtUtil jwt;



    public AuthResponse register(RegisterRequest req){
        if (repo.existsByEmail(req.getEmail()))
            throw new IllegalArgumentException("Email ya registrado");

        Rol role = Rol.CLIENT;
        if (req.getRole()!=null) {
            try { role = Rol.valueOf(req.getRole().toUpperCase()); } catch (Exception ignored) {}
        }

        var u = new appUser();
        u.setEmail(req.getEmail().toLowerCase().trim());
        u.setPasswordHash(encoder.encode(req.getPassword()));
        u.setFullName(req.getFullName());
        u.setRole(role);
        repo.save(u);
        String token = jwt.generate(u.getEmail(), Map.of("role", u.getRole().name()));
        return new AuthResponse(token, u.getEmail(), u.getRole().name());
    }

    public AuthResponse login(LoginRequest req){
        var u = repo.findByEmail(req.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));
        if (!encoder.matches(req.getPassword(), u.getPasswordHash()))
            throw new IllegalArgumentException("Credenciales inválidas");

        String token = jwt.generate(u.getEmail(), Map.of("role", u.getRole().name()));
        return new AuthResponse(token, u.getEmail(), u.getRole().name());
    }
}
