package com.example.blog_be_springboot.sercurity;

public record UserPrincipal(Long id, String username) implements java.security.Principal {
    @Override
    public String getName() {
        return username;
    }

    public Long getId() {
        return id;
    }
}
