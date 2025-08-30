package com.example.blog_be_springboot.service;

import com.example.blog_be_springboot.dto.response.ApiResponse;
import com.example.blog_be_springboot.dto.response.TagResponse;
import com.example.blog_be_springboot.dto.resquest.TagCreateUpdateRequest;
import com.example.blog_be_springboot.entity.Tag;
import com.example.blog_be_springboot.exception.AppException;
import com.example.blog_be_springboot.exception.ErrorCode;
import com.example.blog_be_springboot.mapper.TagMapper;
import com.example.blog_be_springboot.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagService {
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    public ApiResponse<List<TagResponse>> getAllTags(int page, int size) {
        // chặn size quá lớn để tránh DOS
        int p = Math.max(page, 0);
        int s = Math.min(Math.max(size, 1), 100);

        Pageable pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "id"));
        Page<Tag> result = tagRepository.findAll(pageable);

        return tagMapper.toPagedResponse(result); // dùng helper mapper bạn đã có
    }

    public ApiResponse<List<TagResponse>> searchTagsByName(String keyword, int page, int size) {
        // chặn size quá lớn để tránh DOS
        int p = Math.max(page, 0);
        int s = Math.min(Math.max(size, 1), 100);

        Pageable pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "id"));
        Page<Tag> result = tagRepository.searchTagsByNameContainingIgnoreCase(keyword, pageable);

        return tagMapper.toPagedResponse(result);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public TagResponse createTag(TagCreateUpdateRequest tagCreateRequest) {
        Tag tag = new Tag();
        if (tagRepository.existsByName(tagCreateRequest.getTagName())) {
            throw new AppException(ErrorCode.CONFLICT, "Tag name đã tồn tại!");
        }
        tag.setName(tagCreateRequest.getTagName());

        return tagMapper.toTagResponse(tagRepository.save(tag));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public TagResponse updateTag(Long tagId, TagCreateUpdateRequest tagUpdateRequest) {
        Tag tag = tagRepository.findById(tagId).orElseThrow(
                () -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy tag tương ứng!"));

        if (tagRepository.existsByName(tagUpdateRequest.getTagName())) {
            throw new AppException(ErrorCode.CONFLICT, "Tag name không được trùng với tag name hiện tại!");
        }
        tag.setName(tagUpdateRequest.getTagName());

        return tagMapper.toTagResponse(tagRepository.save(tag));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteTag(Long tagId) {
        tagRepository.deleteById(tagId);
    }
}
