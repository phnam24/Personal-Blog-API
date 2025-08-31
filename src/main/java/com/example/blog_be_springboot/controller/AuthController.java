package com.example.blog_be_springboot.controller;

import com.example.blog_be_springboot.dto.response.ApiResponse;
import com.example.blog_be_springboot.dto.response.AuthTokensResponse;
import com.example.blog_be_springboot.dto.response.UserDetailsResponse;
import com.example.blog_be_springboot.dto.request.LoginRequest;
import com.example.blog_be_springboot.dto.request.RegisterRequest;
import com.example.blog_be_springboot.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<AuthTokensResponse> login(@RequestBody LoginRequest loginRequest) {
        return ApiResponse.of(authService.issueTokens(loginRequest), "Login Success");
    }

    @PostMapping("/register")
    public ApiResponse<UserDetailsResponse> register(@RequestBody RegisterRequest registerRequest) {
        return ApiResponse.of(authService.register(registerRequest), "Create Account Success");
    }

    @PatchMapping("/pw-admin/{id}")
    public ApiResponse<String> pwAdmin(@PathVariable Long id, @RequestBody Map<String, String> body) {
        authService.changePassword(id, body.get("password"));
        return ApiResponse.of("Success");
    }

    @PatchMapping("/change-password/{userId}")
    public ApiResponse<UserDetailsResponse> changePassword(@PathVariable Long userId, @RequestBody Map<String, String> body) {

        return ApiResponse.of(authService.changePassword(userId, body.get("oldPassword"), body.get("newPassword"))
                ,"Change Password Success");
    }

    // ==== Refresh ====
    public record RefreshRequest(String refreshToken) {}

    @PostMapping("/refresh")
    public ApiResponse<AuthTokensResponse> refresh(@RequestBody RefreshRequest body) {
        var tokens = authService.rotate(body.refreshToken());
        return ApiResponse.of(tokens);
    }

    // ==== Logout 1 phiên ====
    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestBody RefreshRequest body) {
        authService.logout(body.refreshToken());
        return ApiResponse.of("Logout Success");
    }

    // ==== Logout all ====
    @PostMapping("/logout-all")
    public ApiResponse<String> logoutAll(@RequestParam("userId") Long userId) {
        authService.revokeAll(userId);
        return ApiResponse.of("Logout Success");
    }
}
