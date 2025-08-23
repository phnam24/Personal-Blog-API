package com.example.blog_be_springboot.dto.response;

import org.springframework.data.domain.Page;

public record ApiResponse<T>(T data, String message, Meta meta) {
    // Factory helpers
    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data, "OK", null);
    }

    public static <T> ApiResponse<T> of(T data, String msg) {
        return new ApiResponse<>(data, msg, null);
    }

    public static <T> ApiResponse<T> of(T data, Meta meta) {
        return new ApiResponse<>(data, "OK", meta);
    }

    // Thông tin phụ cho phân trang
    public record Meta(Integer page, Integer size, Long total, Integer totalPages, Boolean hasNext, Boolean hasPrev) {
        public static Meta from(Page<?> p) {
            return new Meta(
                    p.getNumber(),
                    p.getSize(),
                    p.getTotalElements(),
                    p.getTotalPages(),
                    p.hasNext(),
                    p.hasPrevious()
            );
        }
    }
}
