package com.example.blog_be_springboot.mapper;

import com.example.blog_be_springboot.dto.response.ApiResponse;
import com.example.blog_be_springboot.dto.response.TagResponse;
import com.example.blog_be_springboot.entity.Tag;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper( componentModel = "spring")
public interface TagMapper {
    TagResponse toTagResponse(Tag tag);
    Tag toEntity(Tag tag);
    List<TagResponse> toTagResponseList(List<Tag> tags);

    default ApiResponse<List<TagResponse>> toPagedResponse(Page<Tag> page) {
        List<TagResponse> data = toTagResponseList(page.getContent());
        return new ApiResponse<>(data, "OK", ApiResponse.Meta.from(page));
    }
}
