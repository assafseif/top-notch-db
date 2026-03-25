package com.project.service;

import com.project.dto.AppUserDto;
import com.project.dto.LoginResponse;

import java.util.Map;

public interface AuthService {
    LoginResponse login(Map<String, String> loginRequest);
}

