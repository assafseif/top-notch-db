package com.project.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        logger.info("Registering global CORS configuration");
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(false)
                    .maxAge(3600);
            }
        };
    }
}

