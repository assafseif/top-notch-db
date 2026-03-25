package com.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class TokenController {
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(Authentication authentication) {
            return ResponseEntity.ok().build();
    }
}
