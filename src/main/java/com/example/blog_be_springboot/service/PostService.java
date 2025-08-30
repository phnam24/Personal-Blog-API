package com.example.blog_be_springboot.service;

import com.example.blog_be_springboot.dto.response.ApiResponse;
import com.example.blog_be_springboot.dto.response.PostDetailResponse;
import com.example.blog_be_springboot.dto.response.PostSummaryResponse;
import com.example.blog_be_springboot.dto.request.PostCreateRequest;
import com.example.blog_be_springboot.dto.request.PostUpdateRequest;
import com.example.blog_be_springboot.entity.Post;
import com.example.blog_be_springboot.entity.Tag;
import com.example.blog_be_springboot.entity.User;
import com.example.blog_be_springboot.exception.AppException;
import com.example.blog_be_springboot.exception.ErrorCode;
import com.example.blog_be_springboot.helper.DateParsers;
import com.example.blog_be_springboot.helper.PostSpecifications;
import com.example.blog_be_springboot.helper.SortParsers;
import com.example.blog_be_springboot.mapper.PostMapper;
import com.example.blog_be_springboot.repository.PostRepository;
import com.example.blog_be_springboot.repository.TagRepository;
import com.example.blog_be_springboot.repository.UserRepository;
import com.example.blog_be_springboot.sercurity.UserAccess;
import com.example.blog_be_springboot.sercurity.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final PostMapper postMapper;
    private final UserAccess userAccess;

    public ApiResponse<List<PostSummaryResponse>> getAllPostsSummary(int page, int size) {
        // chặn size quá lớn để tránh DOS
        int p = Math.max(page, 0);
        int s = Math.min(Math.max(size, 1), 100);

        Pageable pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> posts = postRepository.findAll(pageable);

        return postMapper.toPagedSummary(posts);
    }

    public PostSummaryResponse getPostSummaryById(Long id) {
        Post post = postRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.POST_NOT_FOUND));

        return postMapper.toPostSummary(post);
    }

    public PostDetailResponse getPostDetailById(Long id) {
        Post post = postRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.POST_NOT_FOUND)
        );

        return postMapper.toPostDetail(post);
    }

    public ApiResponse<List<PostSummaryResponse>> searchPost(
            int page, int size, String sort, String q, Long authorId, Long tagId,
            String createdFrom, String createdTo, String updatedFrom, String updatedTo
    ) {
        int p = Math.max(page - 1, 0);
        int s = Math.min(Math.max(size, 1), 100);
        Sort springSort = SortParsers.parseSort(sort); // allowlist

        Instant cFrom = DateParsers.parseStartInstant(createdFrom);
        Instant cTo   = DateParsers.parseEndInstant(createdTo);
        Instant uFrom = DateParsers.parseStartInstant(updatedFrom);
        Instant uTo   = DateParsers.parseEndInstant(updatedTo);

        Specification<Post> spec = PostSpecifications.build(q, authorId, tagId, cFrom, cTo, uFrom, uTo);
        Page<Post> pageData = postRepository.findAll(spec, PageRequest.of(p, s, springSort));
        return postMapper.toPagedSummary(pageData);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public PostDetailResponse createPost(PostCreateRequest postCreateRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal me = (UserPrincipal) authentication.getPrincipal();
        Long userId = me.getId();

        Post post = new Post();
        post.setTitle(postCreateRequest.getTitle());
        post.setContent(postCreateRequest.getContent());

        User author = userRepository.getReferenceById(userId);
        post.setAuthor(author);

        if (postCreateRequest.getTagIds() != null && !postCreateRequest.getTagIds().isEmpty()) {
            var tags = new HashSet<Tag>();
            for (Long tagId : postCreateRequest.getTagIds()) {
                tags.add(tagRepository.getReferenceById(tagId));
            }
            post.setTags(tags);
        }

        return postMapper.toPostDetail(postRepository.save(post));
    }

    @PreAuthorize("@userAccess.canEdit(#postId, authentication)")
    public PostDetailResponse updatePost(Long postId, PostUpdateRequest postUpdateRequest) {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new AppException(ErrorCode.POST_NOT_FOUND)
        );

        if (postUpdateRequest.getTitle() != null) {
            post.setTitle(postUpdateRequest.getTitle());
        }
        if (postUpdateRequest.getContent() != null) {
            post.setContent(postUpdateRequest.getContent());
        }
        if (postUpdateRequest.getTagIds() != null && !postUpdateRequest.getTagIds().isEmpty()) {
            var tags = new HashSet<Tag>();
            for (Long tagId : postUpdateRequest.getTagIds()) {
                tags.add(tagRepository.getReferenceById(tagId));
            }
            post.setTags(tags);
        }

        return postMapper.toPostDetail(postRepository.save(post));
    }

    @PreAuthorize("@userAccess.canEdit(#postId, authentication)")
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new AppException(ErrorCode.POST_NOT_FOUND)
        );
        postRepository.delete(post);
    }

    @PreAuthorize("@userAccess.canEdit(#postId, authentication)")
    public PostSummaryResponse removeTag(Long postId, Long tagId) {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new AppException(ErrorCode.POST_NOT_FOUND)
        );

        Set<Tag> tags = post.getTags();
        tags.removeIf(tag -> tag.getId().equals(tagId));
        post.setTags(tags);
        return postMapper.toPostSummary(postRepository.save(post));
    }
}
