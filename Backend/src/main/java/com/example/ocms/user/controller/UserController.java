package com.example.ocms.user.controller;

import com.example.ocms.security.UserPrincipal;
import com.example.ocms.user.dto.ProfileUpdateRequest;
import com.example.ocms.user.dto.UserResponse;
import com.example.ocms.user.entity.Role;
import com.example.ocms.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User & Profile Management", description = "Endpoints for viewing and updating user profiles, and admin user queries")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    @Operation(summary = "Get current authenticated user's profile")
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal UserPrincipal currentUser) {
        UserResponse response = userService.getProfile(currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    @Operation(summary = "Update current authenticated user's profile (name and phone number only)")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody ProfileUpdateRequest request) {
        UserResponse response = userService.updateProfile(currentUser, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users with optional role filtering and pagination (Admin only)")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(required = false) Role role,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<UserResponse> users = userService.getAllUsers(role, pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID (Admin or own profile)")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        UserResponse response = userService.getUserById(userId, currentUser);
        return ResponseEntity.ok(response);
    }
}
