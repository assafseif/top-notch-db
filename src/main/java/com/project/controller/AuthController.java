package com.project.controller;

import com.project.dto.LoginResponse;
import com.project.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody Map<String, String> loginRequest) {
        return authService.login(loginRequest);
    }
}
