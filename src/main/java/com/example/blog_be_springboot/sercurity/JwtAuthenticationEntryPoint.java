// src/main/java/com/example/blog_be_springboot/sercurity/JwtAuthenticationEntryPoint.java
package com.example.blog_be_springboot.sercurity;

import com.example.blog_be_springboot.exception.ErrorCode;
import com.example.blog_be_springboot.exception.JwtAuthenticationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper om;

    public JwtAuthenticationEntryPoint(ObjectMapper om) {
        this.om = om;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         org.springframework.security.core.AuthenticationException ex) throws IOException {

        // Mặc định 401 + BAD_CREDENTIALS (bạn có thể đổi sang ErrorCode.UNAUTHORIZED nếu có)
        ErrorCode code = ErrorCode.BAD_CREDENTIALS;
        String msg = ex.getMessage() != null ? ex.getMessage() : "Unauthorized";

        if (ex instanceof JwtAuthenticationException jae) {
            code = jae.getErrorCode();
            msg  = jae.getMessage();
        }

        ProblemDetail body = code.toProblemDetail("Token không hợp lệ!", null);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        om.writeValue(response.getWriter(), body);
    }
}
