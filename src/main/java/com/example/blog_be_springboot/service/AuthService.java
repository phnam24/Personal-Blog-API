package com.example.blog_be_springboot.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.blog_be_springboot.dto.request.LoginRequest;
import com.example.blog_be_springboot.dto.response.AuthTokensResponse;
import com.example.blog_be_springboot.dto.response.UserDetailsResponse;
import com.example.blog_be_springboot.dto.request.RegisterRequest;
import com.example.blog_be_springboot.entity.RefreshToken;
import com.example.blog_be_springboot.entity.Role;
import com.example.blog_be_springboot.entity.User;
import com.example.blog_be_springboot.exception.AppException;
import com.example.blog_be_springboot.exception.ErrorCode;
import com.example.blog_be_springboot.mapper.UserMapper;
import com.example.blog_be_springboot.repository.RefreshTokenRepository;
import com.example.blog_be_springboot.repository.UserRepository;
import com.example.blog_be_springboot.sercurity.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshRepo;
    private final UserMapper userMapper;

    public void changePassword(Long id, String newPassword) {
        Optional<User> user = userRepository.findById(id);
        User userEntity = user.get();
        userEntity.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(userEntity);
    }

    /** Tạo cặp token khi login */
    public AuthTokensResponse issueTokens(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        Long userId = user.getId();

        boolean authenticated = passwordEncoder.matches(password, user.getPassword());

        if (!authenticated) {
            throw new AppException(ErrorCode.BAD_CREDENTIALS);
        }

        String access = jwtService.generateAccessToken(userId, user.getUsername(), user.getRole());

        UUID jti = UUID.randomUUID();
        String refresh = jwtService.generateRefreshToken(userId, user.getUsername(), jti);

        // decode refresh để lấy exp
        Instant exp = jwtService.decodeRefresh(refresh).getExpiresAt().toInstant();

        RefreshToken rt = new RefreshToken();
        rt.setId(jti);
        rt.setUser(user);
        rt.setExpiresAt(exp);
        rt.setRevoked(false);

        refreshRepo.save(rt);

        return new AuthTokensResponse(access, refresh);
    }

    /** Refresh: nhận refresh JWT, rotate, trả cặp token mới */
    public AuthTokensResponse rotate(String refreshJwt) {
        DecodedJWT decoded = jwtService.decodeRefresh(refreshJwt);
        if (!"refresh".equals(decoded.getClaim("typ").asString())) {
            throw new AppException(ErrorCode.BAD_CREDENTIALS, "Not a refresh token");
        }

        String username = decoded.getSubject();
        UUID jti = UUID.fromString(decoded.getId());

        RefreshToken current = refreshRepo.findById(jti)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_CREDENTIALS, "Refresh token not found"));

        // kiểm tra trạng thái
        if (current.isRevoked()) {
            // Reuse detected → có thể revoke all của user
            revokeAll(current.getUser().getId());
            throw new AppException(ErrorCode.BAD_CREDENTIALS, "Refresh token revoked");
        }
        if (current.getExpiresAt().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.BAD_CREDENTIALS, "Refresh token expired");
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        // issue mới
        String newAccess = jwtService.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        UUID newJti = UUID.randomUUID();
        String newRefresh = jwtService.generateRefreshToken(user.getId(), user.getUsername(), newJti);
        Instant newExp = jwtService.decodeRefresh(newRefresh).getExpiresAt().toInstant();

        RefreshToken next = new RefreshToken();
        next.setId(newJti);
        next.setUser(user);
        next.setExpiresAt(newExp);
        next.setRevoked(false);
        refreshRepo.save(next);
        refreshRepo.flush(); // rotate: revoke cái cũ + link tới cái mới

        current.setRevoked(true);
        current.setReplacedBy(newJti);
        current.setLastUsedAt(Instant.now());
        refreshRepo.save(current);


        return new AuthTokensResponse(newAccess, newRefresh);
    }

    /** Logout 1 phiên: revoke refresh hiện tại */
    public void logout(String refreshJwt) {
        DecodedJWT decoded = jwtService.decodeRefresh(refreshJwt);
        UUID jti = UUID.fromString(decoded.getId());
        refreshRepo.findById(jti).ifPresent(rt -> {
            rt.setRevoked(true);
            rt.setLastUsedAt(Instant.now());
            refreshRepo.save(rt);
        });
        // (tuỳ chọn) blacklist access token hiện tại theo jti (nếu bạn thêm jti cho access)
    }

    /** Logout all: revoke mọi refresh token của user */
    public void revokeAll(Long userId) {
        var list = refreshRepo.findByUserId(userId);
        for (RefreshToken t : list) {
            if (!t.isRevoked()) {
                t.setRevoked(true);
                t.setLastUsedAt(Instant.now());
            }
        }
        refreshRepo.saveAll(list);
    }

    public UserDetailsResponse register(RegisterRequest registerRequest) {
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setRole(Role.ROLE_USER);

        return userMapper.toDetailsDto(userRepository.save(user));
    }

    @PreAuthorize("@userAccess.canView(#userId, authentication)")
    public UserDetailsResponse changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new AppException(ErrorCode.BAD_CREDENTIALS, "Sai mật khẩu");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new AppException(ErrorCode.BAD_CREDENTIALS, "Mật khẩu mới trùng với mật khẩu cũ");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        return userMapper.toDetailsDto(userRepository.save(user));
    }
}
