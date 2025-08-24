package com.example.blog_be_springboot.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class JwtAuthenticationException extends AuthenticationException {
    private final ErrorCode errorCode;

    public JwtAuthenticationException(ErrorCode errorCode, String msg) {
        super(msg);
        this.errorCode = errorCode;
    }

}
