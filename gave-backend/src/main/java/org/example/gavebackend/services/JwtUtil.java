package org.example.gavebackend.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {
    private final Key key;
    private final long expMs;

    /**
     * Constructor de JwtUtil
     * @param secret Clave secreta para firmar los tokens
     * @param expMin Tiempo de expiración en minutos (por defecto 120 minutos)
     */
    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.exp-min:120}") long expMin
    ){
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expMs = expMin * 60_000L;
    }

    /**
     * Genera un token JWT
     * @param subject Asunto del token (normalmente el identificador del usuario)
     * @param claims Reclamaciones adicionales a incluir en el token
     * @return Token JWT firmado
     */
    public String generate(String subject, Map<String, Object> claims){
        Date now = new Date();
        Date exp = new Date(now.getTime() + expMs);
        return Jwts.builder()
                .setSubject(subject)
                .addClaims(claims)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Parsea y valida un token JWT
     * @param token Token JWT a parsear
     * @return Objeto Jws con las reclamaciones del token
     */
    public Jws<Claims> parse(String token){
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}
