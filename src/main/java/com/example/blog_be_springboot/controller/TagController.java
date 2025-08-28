package com.example.blog_be_springboot.controller;

import com.example.blog_be_springboot.dto.response.ApiResponse;
import com.example.blog_be_springboot.dto.response.TagResponse;
import com.example.blog_be_springboot.dto.resquest.TagCreateUpdateRequest;
import com.example.blog_be_springboot.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
@Slf4j
public class TagController {
    private final TagService tagService;

    @GetMapping
    public ApiResponse<List<TagResponse>> getAllTags(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return tagService.getAllTags(page, size);
    }

    @GetMapping("/search")
    public ApiResponse<List<TagResponse>> searchTagsByName(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return tagService.searchTagsByName(keyword, page, size);
    }

    @PostMapping("/create")
    public ApiResponse<TagResponse> createTag(@RequestBody TagCreateUpdateRequest tagCreateRequest) {
        return ApiResponse.of(tagService.createTag(tagCreateRequest), "Tag created");
    }

    @PutMapping("/update/{id}")
    public ApiResponse<TagResponse> updateTag(@RequestBody TagCreateUpdateRequest tagCreateRequest, @PathVariable Long id) {
        return ApiResponse.of(tagService.updateTag(id, tagCreateRequest), "Tag updated");
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<String> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ApiResponse.of("Deleted Tag");
    }

}
