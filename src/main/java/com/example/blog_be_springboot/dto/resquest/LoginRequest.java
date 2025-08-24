package com.example.blog_be_springboot.dto.resquest;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    @NotBlank private String username;
    @NotBlank private String password;
}