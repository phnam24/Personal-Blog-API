package com.example.blog_be_springboot.controller;

import com.example.blog_be_springboot.dto.response.ApiResponse;
import com.example.blog_be_springboot.dto.response.PostDetailResponse;
import com.example.blog_be_springboot.dto.response.PostSummaryResponse;
import com.example.blog_be_springboot.dto.request.PostCreateRequest;
import com.example.blog_be_springboot.dto.request.PostUpdateRequest;
import com.example.blog_be_springboot.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {
    private final PostService postService;

    @GetMapping
    public ApiResponse<List<PostSummaryResponse>> getAllPostsSummary(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        return postService.getAllPostsSummary(page, size);
    }

    @GetMapping("/{id}")
    public ApiResponse<PostDetailResponse> getPostById(@PathVariable Long id) {
        return ApiResponse.of(postService.getPostDetailById(id), "Post detail");
    }

    @GetMapping("/search")
    public ApiResponse<List<PostSummaryResponse>> searchPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "created_at,desc") String sort,
            @RequestParam(required = false) String q,
            @RequestParam(name = "author_id", required = false) Long authorId,
            @RequestParam(name = "tag_id", required = false) Long tagId,
            @RequestParam(name = "created_from", required = false) String createdFrom,
            @RequestParam(name = "created_to", required = false) String createdTo,
            @RequestParam(name = "updated_from", required = false) String updatedFrom,
            @RequestParam(name = "updated_to", required = false) String updatedTo
    ) {
        return postService.searchPost(page, size, sort, q, authorId, tagId,
                createdFrom, createdTo, updatedFrom, updatedTo);
    }

    @PostMapping()
    public ApiResponse<PostDetailResponse> createPost(@RequestBody PostCreateRequest postCreateRequest) {
        return ApiResponse.of(postService.createPost(postCreateRequest), "Post created");
    }

    @PatchMapping("/{id}")
    public ApiResponse<PostDetailResponse> updatePost(
            @PathVariable Long id,
            @RequestBody PostUpdateRequest postUpdateRequest) {
        return ApiResponse.of(postService.updatePost(id, postUpdateRequest), "Post updated");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ApiResponse.of("Deleted post");
    }

    @PatchMapping("/{id}/tags/{tagId}")
    public ApiResponse<PostSummaryResponse> updatePostTag(@PathVariable Long id, @PathVariable Long tagId) {
        return ApiResponse.of(postService.removeTag(id, tagId), "Post tag removed");
    }
}
