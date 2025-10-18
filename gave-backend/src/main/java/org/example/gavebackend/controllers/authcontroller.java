package org.example.gavebackend.controllers;

import jakarta.validation.Valid;
import org.example.gavebackend.dtos.AuthResponse;
import org.example.gavebackend.dtos.LoginRequest;
import org.example.gavebackend.dtos.RegisterRequest;
import org.example.gavebackend.services.impl.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class authcontroller {

    @Autowired
    private AuthService service;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req){
        return service.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req){
        return service.login(req);
    }

    // opcional: /me si querés devolver email/rol del token (el filtro ya setea SecurityContext)
}
