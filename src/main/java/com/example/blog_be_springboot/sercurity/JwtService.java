package com.example.blog_be_springboot.sercurity;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.blog_be_springboot.entity.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    private final Algorithm accessAlg;
    private final JWTVerifier accessVerifier;
    private final String issuer;
    private final long accessExpMinutes;

    // refresh
    private final Algorithm refreshAlg;
    private final JWTVerifier refreshVerifier;
    private final long refreshExpMinutes;

    public JwtService(
            @Value("${app.jwt.secret}") String accessSecret,
            @Value("${app.jwt.refresh-secret:change-me-refresh-secret}") String refreshSecret,
            @Value("${app.jwt.issuer:blog-app}") String issuer,
            @Value("${app.jwt.exp-minutes:15}") long accessExpMinutes,
            @Value("${app.jwt.refresh-exp-minutes:43200}") long refreshExpMinutes // 30 ngày
    ) {
        this.accessAlg = Algorithm.HMAC256(accessSecret);
        this.refreshAlg = Algorithm.HMAC256(refreshSecret);
        this.issuer = issuer;
        this.accessExpMinutes = accessExpMinutes;
        this.refreshExpMinutes = refreshExpMinutes;

        this.accessVerifier = JWT.require(accessAlg).withIssuer(issuer).build();
        this.refreshVerifier = JWT.require(refreshAlg).withIssuer(issuer).build();
    }

    // ===== Access Token =====
    public String generateAccessToken(Long userId, String username, Role role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessExpMinutes * 60_000L);
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(username)
                .withClaim("role", role.name())
                .withIssuedAt(now)
                .withExpiresAt(exp)
                .withJWTId(UUID.randomUUID().toString()) // jti (tiện blacklist)
                .withClaim("typ", "access")
                .withClaim("user_id", userId)
                .sign(accessAlg);
    }

    public DecodedJWT decodeAccess(String token) {
        return accessVerifier.verify(token);
    }

    public boolean validateAccessToken(String token) {
        try {
            accessVerifier.verify(token);
            return true;
        } catch (JWTVerificationException ex) {
            return false;
        }
    }

    public void assertValid(String token) throws JWTVerificationException {
        accessVerifier.verify(token); // sẽ ném TokenExpiredException / JWTVerificationException ...
    }

    public Long getUserId(String token) {
        try {
            return decodeAccess(token).getClaim("user_id").asLong();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    public String getUsername(String token) {
        try {
            return decodeAccess(token).getSubject();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    public String getRole(String token) {
        try {
            DecodedJWT jwt = decodeAccess(token);
            return jwt.getClaim("role").asString();
        } catch (JWTVerificationException e) {
            return "";
        }
    }

    public Date getExpiry(String token) {
        try {
            return decodeAccess(token).getExpiresAt();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    public String generateRefreshToken(Long userId, String username, UUID jti) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshExpMinutes * 60_000L);
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(username)
                .withIssuedAt(now)
                .withExpiresAt(exp)
                .withJWTId(jti.toString())   // <- quan trọng
                .withClaim("typ", "refresh")
                .withClaim("user_id", userId)// đánh dấu loại token
                .sign(refreshAlg);
    }

    public DecodedJWT decodeRefresh(String refreshJwt) {
        return refreshVerifier.verify(refreshJwt);
    }
}
