// src/main/java/com/example/blog_be_springboot/sercurity/JwtAuthenticationFilter.java
package com.example.blog_be_springboot.sercurity;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.example.blog_be_springboot.exception.ErrorCode;
import com.example.blog_be_springboot.exception.JwtAuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHZ = "Authorization";
    private static final String BEARER = "Bearer ";
    private static final AntPathMatcher PATH = new AntPathMatcher();

    private final JwtService jwtService;

    // whitelist endpoint public
    private final Set<String> whitelist = Set.of(
            "/auth/login",
            "/auth/register",
            "/swagger-ui", "/swagger-ui/",
            "/v3/api-docs", "/v3/api-docs/",
            "/public"
    );

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /** BỎ QUA filter cho các path công khai hoặc preflight CORS */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true; // CORS preflight
        String path = request.getServletPath(); // không gồm context-path
        for (String pattern : whitelist) {
            if (PATH.match(pattern, path)) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(AUTHZ);
        if (header == null || !header.startsWith(BEARER)) {
            // Thiếu/malformed header → ném AuthenticationException
            throw new JwtAuthenticationException(
                    ErrorCode.BAD_CREDENTIALS, "Missing or malformed Authorization header");
        }

        String token = header.substring(BEARER.length()).trim();

        // Xác thực token & phân biệt lỗi
        try {
            jwtService.assertValid(token); // sẽ ném nếu invalid/expired
        } catch (TokenExpiredException e) {
            throw new JwtAuthenticationException(ErrorCode.BAD_CREDENTIALS, "Token expired");
        } catch (JWTVerificationException e) {
            throw new JwtAuthenticationException(ErrorCode.BAD_CREDENTIALS, "Invalid token");
        }

        // Lấy username + role từ token
        String username = jwtService.getUsername(token);
        String role = jwtService.getRole(token); // kỳ vọng trả về "ADMIN", "USER", ...

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Map sang authority "ROLE_..."
            String authority = (role != null && !role.isBlank())
                    ? (role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    : null;

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            authority == null ? List.of() : List.of(new SimpleGrantedAuthority(authority))
                    );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
