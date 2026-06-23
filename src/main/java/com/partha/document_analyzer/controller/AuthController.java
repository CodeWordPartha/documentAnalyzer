package com.partha.document_analyzer.controller;

import com.partha.document_analyzer.dto.LoginRequestDto;
import com.partha.document_analyzer.dto.RegisterRequestDto;
import com.partha.document_analyzer.dto.SuccessResponseDto;
import com.partha.document_analyzer.dto.UserResponseDto;
import com.partha.document_analyzer.services.UserService;
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

    @PostMapping("/register")
    public ResponseEntity<SuccessResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        UserResponseDto user = userService.registerUser(request);
        SuccessResponseDto response = new SuccessResponseDto("user registered succesfully", user);

        return new ResponseEntity<>(response, HttpStatus.CREATED);

    }

    @PostMapping("/login")
    public ResponseEntity<SuccessResponseDto> login (@Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(new SuccessResponseDto("Login endpoint - Coming soon!"));

    }
}
