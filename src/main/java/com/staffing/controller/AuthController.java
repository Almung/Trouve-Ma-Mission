package com.staffing.controller;

import com.staffing.dto.ApiResponse;
import com.staffing.dto.AuthResponseDTO;
import com.staffing.dto.LoginDTO;
import com.staffing.dto.UserDTO;
import com.staffing.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@Valid @RequestBody LoginDTO loginDTO) {
        AuthResponseDTO response = authService.login(loginDTO);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> refreshToken() {
        AuthResponseDTO response = authService.refreshToken();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Map<String, String>>> logout() {
        authService.logout();
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Déconnexion réussie")));
    }

    @GetMapping("/check-auth")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkAuth() {
        Map<String, Object> response = authService.checkAuthStatus();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/permissions/{entity}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> getPermissions(@PathVariable String entity) {
        Map<String, Boolean> permissions = authService.getPermissionsForEntity(entity);
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    @GetMapping("/user-access/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkUserAccess(@PathVariable Long userId) {
        Map<String, Boolean> access = authService.checkUserAccess(userId);
        return ResponseEntity.ok(ApiResponse.success(access));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        UserDTO user = authService.getCurrentUserDTO();
        return ResponseEntity.ok(ApiResponse.success(user));
    }
} 