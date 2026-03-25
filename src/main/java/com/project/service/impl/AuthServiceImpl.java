package com.project.service.impl;

import com.project.dto.AppUserDto;
import com.project.dto.LoginResponse;
import com.project.entity.AppUser;
import com.project.repository.AppUserRepository;
import com.project.security.JwtTokenProvider;
import com.project.service.AuthService;
import com.project.service.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AppUserService appUserService;

    @Override
    public LoginResponse login(Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(userDetails);
        AppUser user = appUserRepository.findByUsername(username);
        AppUserDto dto = appUserService.getById(user.getId());
        return new LoginResponse(token, dto);
    }
}

