package com.project.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class WebConfig {
    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);
    private final List<String> allowedOrigins;

    public WebConfig(@Value("${app.cors.allowed-origins:http://109.205.180.47:89}") String allowedOrigins) {
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
            .map(String::trim)
            .filter(origin -> !origin.isEmpty())
            .toList();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        logger.info("Registering global CORS configuration for origins: {}", allowedOrigins);
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                var registration = registry.addMapping("/api/**")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(false)
                    .maxAge(3600);

                if (allowedOrigins.size() == 1 && "*".equals(allowedOrigins.get(0))) {
                    registration.allowedOriginPatterns("*");
                    return;
                }

                registration.allowedOrigins(allowedOrigins.toArray(String[]::new));
            }
        };
    }
}

