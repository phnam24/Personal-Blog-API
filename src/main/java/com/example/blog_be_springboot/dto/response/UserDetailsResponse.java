package com.example.blog_be_springboot.dto.response;

import com.example.blog_be_springboot.entity.Role;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailsResponse {
    private Long id;
    private String username;
    private String email;
    private LocalDate creationDate;
    private Role role;
}
