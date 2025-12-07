package org.example.gavebackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.example.gavebackend.dtos.*;
import org.example.gavebackend.entities.enums.Rol;
import org.example.gavebackend.entities.appUser;
import org.example.gavebackend.repositories.appUserRepository;
import org.example.gavebackend.services.JwtUtil;
import org.example.gavebackend.services.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final appUserRepository repo;

    private final PasswordEncoder encoder;

    private final JwtUtil jwt;

    private final MailService mail;

    @Value("${app.frontend.base-url}")
    private String frontendBase;

    /**
     * Registro de nuevo usuario
     * @param req Datos de registro
     * @return Token JWT y datos del usuario
     */
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


        //try { mail.sendWelcomeEmail(u.getEmail(), u.getFullName()); } catch (Exception ignored){}

        String token = jwt.generate(u.getEmail(), Map.of("role", u.getRole().name()));
        return new AuthResponse(token, u.getEmail(), u.getRole().name());
    }

    /**
     * Login de usuario
     * @param req Datos de login
     * @return Token JWT y datos del usuario
     */
    public AuthResponse login(LoginRequest req){
        var u = repo.findByEmail(req.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));
        if (!encoder.matches(req.getPassword(), u.getPasswordHash()))
            throw new IllegalArgumentException("Credenciales inválidas");

        String token = jwt.generate(u.getEmail(), Map.of("role", u.getRole().name()));
        return new AuthResponse(token, u.getEmail(), u.getRole().name());
    }

    /**
     * Solicitar reseteo de contraseña
     * @param req Datos de solicitud
     */
    public void forgot(ForgotPasswordRequest req) {
        String email = req.getEmail().toLowerCase().trim();
        Optional<appUser> opt = repo.findByEmail(email);


        if (opt.isEmpty()) return;

        var user = opt.get();
        String token = UUID.randomUUID().toString().replace("-", "");
        user.setPasswordResetToken(token);
        user.setPasswordResetExpiry(Instant.now().plus(2, ChronoUnit.HOURS));
        repo.save(user);

        String link = frontendBase + "/reset?token=" + token;
        try { mail.sendPasswordResetEmail(user.getEmail(), user.getFullName(), link, 2); } catch (Exception ignored){}
    }

    /**
     * Resetear contraseña
     * @param req Datos de reseteo
     */
    public void reset(ResetPasswordRequest req) {
        var user = repo.findByPasswordResetToken(req.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Token inválido"));

        if (user.getPasswordResetExpiry()==null || user.getPasswordResetExpiry().isBefore(Instant.now()))
            throw new IllegalArgumentException("Token expirado");

        user.setPasswordHash(encoder.encode(req.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        repo.save(user);
    }
}