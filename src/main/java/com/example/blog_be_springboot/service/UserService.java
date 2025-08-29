package com.example.blog_be_springboot.service;

import com.example.blog_be_springboot.dto.response.ApiResponse;
import com.example.blog_be_springboot.dto.response.UserDetailsResponse;
import com.example.blog_be_springboot.dto.resquest.UserUpdateRequest;
import com.example.blog_be_springboot.entity.User;
import com.example.blog_be_springboot.exception.AppException;
import com.example.blog_be_springboot.exception.ErrorCode;
import com.example.blog_be_springboot.mapper.UserMapper;
import com.example.blog_be_springboot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserDetailsResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name);

        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        return userMapper.toDetailsDto(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<UserDetailsResponse>> getAllUsers(int page, int size) {
        // chặn size quá lớn để tránh DOS
        int p = Math.max(page, 0);
        int s = Math.min(Math.max(size, 1), 100);

        Pageable pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "createdAt")); // hoặc "id"
        Page<User> result = userRepository.findAll(pageable);

        return userMapper.toPagedResponse(result); // dùng helper mapper bạn đã có
    }

    public ApiResponse<List<UserDetailsResponse>> searchUsers(String keyword, int page, int size) {
        int p = Math.max(page, 0);
        int s = Math.min(Math.max(size, 1), 100);

        Pageable pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "id"));
        Page<User> result = userRepository.findAll(pageable);

        return userMapper.toPagedResponse(result);
    }

    @PreAuthorize("@userAccess.canView(#userId, authentication)")
    public UserDetailsResponse getUserById(Long userId) {
        return userMapper.toDetailsDto(userRepository.findById(userId).
                orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
    }

    @PreAuthorize("@userAccess.canView(#userId, authentication)")
    public UserDetailsResponse updateUser(Long userId, UserUpdateRequest userUpdateRequest) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (user.getEmail().equals(userUpdateRequest.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_USED);
        }
        user.setEmail(userUpdateRequest.getEmail());
        System.out.println(userMapper.toDetailsDto(user));
        return userMapper.toDetailsDto(userRepository.save(user));
    }

    @PreAuthorize("@userAccess.canView(#userId, authentication)")
    public UserDetailsResponse deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
        return userMapper.toDetailsDto(user);
    }
}