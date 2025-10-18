package org.example.gavebackend.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @Email
    @NotBlank
    private String email;
    @NotBlank private String password;
    private String fullName;
    private String role; // "ADMIN" | "CLIENT" (opcional, default CLIENT)
}
