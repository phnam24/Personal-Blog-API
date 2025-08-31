package com.example.blog_be_springboot.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthTokensResponse {
    private String accessToken;
    private String refreshToken;
}
