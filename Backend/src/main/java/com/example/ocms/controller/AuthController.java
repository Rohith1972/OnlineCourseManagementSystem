package com.example.ocms.controller;

import com.example.ocms.dto.AuthResponse;
import com.example.ocms.dto.InstructorRegisterRequest;
import com.example.ocms.dto.LoginRequest;
import com.example.ocms.dto.StudentRegisterRequest;
import com.example.ocms.service.AuthService;
import com.example.ocms.security.UserPrincipal;
import com.example.ocms.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration, login, and current user retrieval")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/student")
    @Operation(summary = "Register a new student")
    public ResponseEntity<UserResponse> registerStudent(@Valid @RequestBody StudentRegisterRequest request) {
        UserResponse response = authService.registerStudent(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/register/instructor")
    @Operation(summary = "Register a new instructor")
    public ResponseEntity<UserResponse> registerInstructor(@Valid @RequestBody InstructorRegisterRequest request) {
        UserResponse response = authService.registerInstructor(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and generate JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user details", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserResponse response = authService.getCurrentUser(userPrincipal);
        return ResponseEntity.ok(response);
    }
}
