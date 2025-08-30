package com.example.blog_be_springboot.controller;

import com.example.blog_be_springboot.dto.response.ApiResponse;
import com.example.blog_be_springboot.dto.response.LoginResponse;
import com.example.blog_be_springboot.dto.response.UserDetailsResponse;
import com.example.blog_be_springboot.dto.request.LoginRequest;
import com.example.blog_be_springboot.dto.request.RegisterRequest;
import com.example.blog_be_springboot.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return ApiResponse.of(authService.login(loginRequest), "Login Success");
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
}
