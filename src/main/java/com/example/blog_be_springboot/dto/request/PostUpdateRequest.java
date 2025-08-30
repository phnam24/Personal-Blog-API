package com.example.blog_be_springboot.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateRequest {
    @Size(min = 1, max = 255)
    private String title;
    private String content;
    private List<Long> tagIds;
}