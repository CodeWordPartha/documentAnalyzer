package com.partha.document_analyzer.controller;

import com.partha.document_analyzer.dto.*;
import com.partha.document_analyzer.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "Register new user")
    @PostMapping("/register")
    public ResponseEntity<SuccessResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        UserResponseDto user = userService.registerUser(request);
        SuccessResponseDto response = new SuccessResponseDto("user registered succesfully", user);

        return new ResponseEntity<>(response, HttpStatus.CREATED);

    }

    @Operation(summary = "Login", description = "Returns JWT token to use in other APIs")
    @PostMapping("/login")
    public ResponseEntity<SuccessResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        UserLoginResponseDto response = userService.loginUser(request);
        return ResponseEntity.ok(new SuccessResponseDto("Login successful", response));
    }
}
