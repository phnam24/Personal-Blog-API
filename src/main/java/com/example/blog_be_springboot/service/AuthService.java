package com.example.blog_be_springboot.service;

import com.example.blog_be_springboot.dto.response.LoginResponse;
import com.example.blog_be_springboot.dto.response.UserDetailsResponse;
import com.example.blog_be_springboot.dto.resquest.LoginRequest;
import com.example.blog_be_springboot.dto.resquest.RegisterRequest;
import com.example.blog_be_springboot.entity.Role;
import com.example.blog_be_springboot.entity.User;
import com.example.blog_be_springboot.exception.AppException;
import com.example.blog_be_springboot.exception.ErrorCode;
import com.example.blog_be_springboot.mapper.UserMapper;
import com.example.blog_be_springboot.repository.UserRepository;
import com.example.blog_be_springboot.sercurity.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
    private final JwtService jwtService;
    private final UserMapper userMapper;

    public void changePassword(Long id, String newPassword) {
        Optional<User> user = userRepository.findById(id);
        User userEntity = user.get();
        userEntity.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(userEntity);
    }

    public LoginResponse login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        User user = userRepository.findByUsername(username);
        System.out.println(username + " " + password);
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        boolean authenticated = passwordEncoder.matches(password, user.getPassword());

        if (!authenticated) {
            throw new AppException(ErrorCode.BAD_CREDENTIALS);
        }

        String token = jwtService.generateToken(username, user.getRole());
        return new LoginResponse(token);
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
