package org.example.gavebackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gaveOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gave Mini-ecommerce API")
                        .version("v1")
                        .description("API del mini ecommerce (productos, imágenes, auth)"))
                // definir el scheme en Components
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                // requisito global: todas las operaciones llevan candado por defecto
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
