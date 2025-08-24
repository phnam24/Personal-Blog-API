package com.example.blog_be_springboot.dto.resquest;

import com.example.blog_be_springboot.entity.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    @NotBlank
    @Size(min = 2, max = 50, message = "INVALID_USERNAME")
    private String username;
    @NotBlank
    @Size(min = 6, max = 50, message = "INVALID_PASSWORD")
    private String password;
    @NotBlank
    @Email
    private String email;
    private Role role = Role.ROLE_USER;
}
