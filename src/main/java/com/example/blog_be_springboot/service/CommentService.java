package com.example.blog_be_springboot.service;

import com.example.blog_be_springboot.dto.request.CommentCreateRequest;
import com.example.blog_be_springboot.dto.request.CommentUpdateRequest;
import com.example.blog_be_springboot.dto.response.ApiResponse;
import com.example.blog_be_springboot.dto.response.CommentResponse;
import com.example.blog_be_springboot.dto.response.CommentTreeResponse;
import com.example.blog_be_springboot.entity.Comment;
import com.example.blog_be_springboot.entity.Post;
import com.example.blog_be_springboot.entity.User;
import com.example.blog_be_springboot.exception.AppException;
import com.example.blog_be_springboot.exception.ErrorCode;
import com.example.blog_be_springboot.mapper.CommentMapper;
import com.example.blog_be_springboot.repository.CommentRepository;
import com.example.blog_be_springboot.repository.PostRepository;
import com.example.blog_be_springboot.repository.UserRepository;
import com.example.blog_be_springboot.sercurity.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    public CommentResponse getCommentById(Long commentId) {
        return commentMapper.toCommentResponse(commentRepository.findById(commentId).get());
    }

    public ApiResponse<List<CommentResponse>> getAllCommentsByPostId(Long postId, int page, int size) {
        int p = Math.max(page, 0);
        int s = Math.min(Math.max(size, 1), 100);

        Pageable pageable = PageRequest.of(p, s, Sort.by("createdAt").descending());
        Page<Comment> comments = commentRepository.findAllByPost_Id(postId, pageable);

        return commentMapper.toPagedResponse(comments);
    }

    public ApiResponse<List<CommentTreeResponse>> getCommentsTreeByPostId(Long postId, int page, int size) {
        int p = Math.max(page, 0);
        int s = Math.min(Math.max(size, 1), 100);

        Pageable pageable = PageRequest.of(p, s, Sort.by("createdAt").descending());
        Page<Comment> comments = commentRepository.findAllByPost_Id(postId, pageable);

        return commentMapper.toPagedTreeResponse(comments);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public CommentResponse createComment(Long postId, CommentCreateRequest commentCreateRequest) {
        Comment comment = new Comment();

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new AppException(ErrorCode.POST_NOT_FOUND)
        );
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal me = (UserPrincipal) authentication.getPrincipal();
        String username = me.getName();
        User author = userRepository.findByUsername(username);

        if (commentCreateRequest.getParentId() != null) {
            Comment parentComment = commentRepository.findById(commentCreateRequest.getParentId()).get();
            comment.setParent(parentComment);
            Set<Comment> children = parentComment.getChildren();
            children.add(comment);
            parentComment.setChildren(children);
        }
        else {
            comment.setParent(null);
        }
        comment.setContent(commentCreateRequest.getContent());
        comment.setPost(post);
        comment.setUser(author);

        return commentMapper.toCommentResponse(commentRepository.save(comment));
    }

    @PreAuthorize("@userAccess.canEditComment(#commentId, authentication)")
    public CommentResponse updateComment(Long commentId, CommentUpdateRequest commentUpdateRequest) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new AppException(ErrorCode.COMMENT_NOT_FOUND)
        );

        comment.setContent(commentUpdateRequest.getContent());
        return commentMapper.toCommentResponse(commentRepository.save(comment));
    }

    @PreAuthorize("@userAccess.canEditComment(#commentId, authentication)")
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new AppException(ErrorCode.COMMENT_NOT_FOUND)
        );

        Comment parentComment = comment.getParent();
        if (parentComment != null) {
            Set<Comment> children = parentComment.getChildren();
            children.remove(comment);
            parentComment.setChildren(children);
        }

        commentRepository.delete(comment);
    }
}
