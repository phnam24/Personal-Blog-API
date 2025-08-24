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

@Service
public class JwtService {
    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final String issuer;
    private final long expiryMinutes;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.issuer:blog-app}") String issuer,
            @Value("${app.jwt.exp-minutes:60}") long expiryMinutes
    ) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.issuer = issuer;
        this.expiryMinutes = expiryMinutes;
        this.verifier = JWT.require(algorithm).withIssuer(issuer).build();
    }

    public String generateToken(String username, Role roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiryMinutes * 60_000L);

        return JWT.create()
                .withIssuer(issuer)
                .withSubject(username)
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .withClaim("role", roles.name())
                .sign(algorithm);
    }

    private DecodedJWT decode(String token) throws JWTVerificationException {
        return verifier.verify(token);
    }

    public boolean validateToken(String token) {
        try {
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException ex) {
            return false;
        }
    }

    public void assertValid(String token) throws JWTVerificationException {
        verifier.verify(token); // sẽ ném TokenExpiredException / JWTVerificationException ...
    }

    public String getUsername(String token) {
        try {
            return decode(token).getSubject();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    public String getRole(String token) {
        try {
            DecodedJWT jwt = decode(token);
            return jwt.getClaim("role").asString();
        } catch (JWTVerificationException e) {
            return "";
        }
    }

    public Date getExpiry(String token) {
        try {
            return decode(token).getExpiresAt();
        } catch (JWTVerificationException e) {
            return null;
        }
    }
}
