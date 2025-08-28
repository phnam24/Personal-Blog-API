package com.example.blog_be_springboot.controller;

import com.example.blog_be_springboot.dto.response.ApiResponse;
import com.example.blog_be_springboot.dto.response.UserDetailsResponse;
import com.example.blog_be_springboot.dto.resquest.UserUpdateRequest;
import com.example.blog_be_springboot.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping("/myInfo")
    public ApiResponse<UserDetailsResponse> getMyInfo(){
        return ApiResponse.of(userService.getMyInfo());
    }

    @GetMapping
    public ApiResponse<List<UserDetailsResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return userService.getAllUsers(page, size);
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserDetailsResponse> getUserById(@PathVariable Long userId){
        return ApiResponse.of(userService.getUserById(userId));
    }

    @PatchMapping("/update/{userId}")
    public ApiResponse<UserDetailsResponse> updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateRequest userUpdateRequest
    ){
        return ApiResponse.of(userService.updateUser(userId, userUpdateRequest)
        ,"Update Account Success");
    }

    @DeleteMapping("/delete/{userId}")
    public ApiResponse<UserDetailsResponse> deleteUser(@PathVariable Long userId){
        return ApiResponse.of(userService.deleteUser(userId), "Delete Account Success");
    }
}
