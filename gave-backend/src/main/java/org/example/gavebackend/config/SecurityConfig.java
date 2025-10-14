package org.example.gavebackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())     // API stateless; si usás cookies, ajustá esto
                .cors(Customizer.withDefaults())  // usa el CorsConfigurationSource de abajo
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // preflight
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .anyRequest().permitAll() // ajustá según tu auth
                );
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();

        // Frontend dev (Angular):
        cors.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "http://127.0.0.1:4200"
                // agregá aquí tu dominio en prod, ej:
                // "https://app.midominio.com"
        ));

        cors.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization","Content-Type","Accept","Origin","X-Requested-With"));
        cors.setExposedHeaders(List.of("Location")); // si devolvés Location en 201 CREATED
        cors.setAllowCredentials(true);              // ponelo en false si no usás cookies
        cors.setMaxAge(3600L);                       // cachea preflight 1 hora

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplica a toda tu API:
        source.registerCorsConfiguration("/**", cors);
        return source;
    }
}