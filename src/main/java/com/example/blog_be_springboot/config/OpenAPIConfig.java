package com.example.blog_be_springboot.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.*;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.security.*;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Personal Blog API",
                version = "v1",
                description = "API tài liệu cho blog",
                contact = @Contact(name = "Team", email = "dev@example.com")
        ),
        servers = {
                @Server(url = "http://localhost:8080/personal_blog_api/api", description = "Local"),
                @Server(url = "https://api.example.com", description = "Prod")
        },
        security = { @SecurityRequirement(name = "bearerAuth") } // áp cho mọi endpoint (có thể bỏ)
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@Configuration
public class OpenAPIConfig { }
