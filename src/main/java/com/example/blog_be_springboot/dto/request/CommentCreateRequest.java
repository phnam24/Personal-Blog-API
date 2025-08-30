package com.example.blog_be_springboot.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequest {
    @NotBlank
    private String content;

    /**
     * Optional: reply vào comment khác; post_id lấy từ path; user_id lấy từ JWT
     */
    private Long parentId;
}
