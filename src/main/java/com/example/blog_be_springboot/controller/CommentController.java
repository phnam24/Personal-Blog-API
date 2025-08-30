package com.example.blog_be_springboot.controller;

import com.example.blog_be_springboot.dto.request.CommentCreateRequest;
import com.example.blog_be_springboot.dto.request.CommentUpdateRequest;
import com.example.blog_be_springboot.dto.response.ApiResponse;
import com.example.blog_be_springboot.dto.response.CommentResponse;
import com.example.blog_be_springboot.dto.response.CommentTreeResponse;
import com.example.blog_be_springboot.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/comments/{id}")
    public ApiResponse<CommentResponse> getCommentById(@PathVariable Long id) {
        return ApiResponse.of(commentService.getCommentById(id));
    }

    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<List<CommentResponse>> getAllCommentsByPostId(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return commentService.getAllCommentsByPostId(postId, page, size);
    }

    @GetMapping("/posts/{postId}/comments/tree")
    public ApiResponse<List<CommentTreeResponse>> getCommentsTreeByPostId(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return commentService.getCommentsTreeByPostId(postId, page, size);
    }

    @PostMapping("/posts/{postId}/comments")
    public ApiResponse<CommentResponse> createComment(
            @PathVariable Long postId,
            @RequestBody CommentCreateRequest commentCreateRequest
    ) {
        return ApiResponse.of(commentService.createComment(postId, commentCreateRequest), "Comment created");
    }

    @PatchMapping("/comments/{commentId}")
    public ApiResponse<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentUpdateRequest commentUpdateRequest
    ) {
        return ApiResponse.of(commentService.updateComment(commentId, commentUpdateRequest), "Comment updated");
    }

    @DeleteMapping("/comments/{commentId}")
    public ApiResponse<String> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ApiResponse.of("Comment deleted");
    }
}
