package org.example.gavebackend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.gavebackend.dtos.*;
import org.example.gavebackend.services.impl.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class authcontroller {

    private final AuthService service;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req){

        return service.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req){

        return service.login(req);
    }

    @PostMapping("/forgot")
    public void forgot(@Valid @RequestBody ForgotPasswordRequest req){

        service.forgot(req);
    }

    @PostMapping("/reset")
    public void reset(@Valid @RequestBody ResetPasswordRequest req){

        service.reset(req);
    }
}
