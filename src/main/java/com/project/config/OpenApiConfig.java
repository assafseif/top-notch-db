package com.project.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Top Notch Backend API",
                version = "1.0.0",
                description = "REST API documentation for Top Notch backend",
                contact = @Contact(name = "Top Notch", email = "support@topnotch.example"),
                license = @License(name = "MIT")
        )
)
public class OpenApiConfig {
    // Empty - annotations configure OpenAPI metadata
}

