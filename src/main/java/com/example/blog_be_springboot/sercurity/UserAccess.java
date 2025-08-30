package com.example.blog_be_springboot.sercurity;

import com.example.blog_be_springboot.entity.Comment;
import com.example.blog_be_springboot.entity.Post;
import com.example.blog_be_springboot.exception.AppException;
import com.example.blog_be_springboot.exception.ErrorCode;
import com.example.blog_be_springboot.repository.CommentRepository;
import com.example.blog_be_springboot.repository.PostRepository;
import com.example.blog_be_springboot.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("userAccess")
public class UserAccess {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public UserAccess(
            UserRepository userRepository,
            PostRepository postRepository,
            CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    public boolean canView(Long id, Authentication auth) {
        if (auth == null) return false;
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return true;

        String me = auth.getName();
        return userRepository.findById(id)
                .map(u -> u.getUsername().equals(me))
                .orElse(false);
    }

    public boolean canEdit(Long postId, Authentication auth) {
        if (auth == null) return false;
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return true;

        UserPrincipal me = (UserPrincipal) auth.getPrincipal();
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new AppException(ErrorCode.POST_NOT_FOUND)
        );
        boolean isAuthor = me.getId().equals(post.getAuthor().getId());
        if (!isAuthor) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        return true;
    }

    public boolean canEditComment(Long commentId, Authentication auth) {
        if (auth == null) return false;
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return true;

        UserPrincipal me = (UserPrincipal) auth.getPrincipal();
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new AppException(ErrorCode.COMMENT_NOT_FOUND)
        );
        boolean isAuthor = me.getId().equals(comment.getUser().getId());
        if (!isAuthor) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        return true;
    }
}
