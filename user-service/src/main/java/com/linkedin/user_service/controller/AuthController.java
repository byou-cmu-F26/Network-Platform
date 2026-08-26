package com.linkedin.user_service.controller;

import com.linkedin.user_service.dto.AuthResponse;
import com.linkedin.user_service.dto.LoginRequest;
import com.linkedin.user_service.dto.RegisterRequest;
import com.linkedin.user_service.dto.UserResponse;
import com.linkedin.user_service.service.AuthService;
import com.linkedin.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    // 注册接口
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest){
        log.info("Register request: {}",registerRequest.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(registerRequest));
    }


    // 登陆接口
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest){
        log.info("Login Request: {}",loginRequest.getEmail());
        return ResponseEntity.ok(authService.login(loginRequest));
    }



}