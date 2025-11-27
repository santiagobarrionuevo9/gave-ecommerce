package org.example.gavebackend.dtos;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String email;
    private String role;
    public AuthResponse(String t, String e, String r)
    { token=t; email=e; role=r; }
}
