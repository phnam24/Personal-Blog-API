package com.example.blog_be_springboot.mapper;

import com.example.blog_be_springboot.dto.response.ApiResponse;
import com.example.blog_be_springboot.dto.response.PostDetailResponse;
import com.example.blog_be_springboot.dto.response.PostSummaryResponse;
import com.example.blog_be_springboot.dto.resquest.PostCreateRequest;
import com.example.blog_be_springboot.dto.resquest.PostUpdateRequest;
import com.example.blog_be_springboot.entity.Post;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper( componentModel = "spring",
        uses = {TagMapper.class, UserMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostMapper {
    @Mapping(source = "author", target = "author") // UserMapper.toAuthorBrief
    @Mapping(source = "tags", target = "tags")     // TagMapper: Set<Tag> -> List<TagResponse>
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "instantToOffsetDateTimeUtc")
    PostSummaryResponse toPostSummary(Post post);

    List<PostSummaryResponse> toSummaryList(List<Post> list);

    @Mapping(source = "author", target = "author")
    @Mapping(source = "tags", target = "tags")
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "instantToOffsetDateTimeUtc")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "instantToOffsetDateTimeUtc")
    PostDetailResponse toPostDetail(Post post);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)     // set ở service từ JWT
    @Mapping(target = "tags", ignore = true)       // xử lý tại service
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "createdAt", ignore = true)  // @PrePersist
    @Mapping(target = "updatedAt", ignore = true)  // @PrePersist/@PreUpdate
    Post toPostEntity(PostCreateRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "tags", ignore = true)       // cập nhật tags qua service riêng
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)  // DB sẽ tự cập nhật qua @PreUpdate
    void update(@MappingTarget Post e, PostUpdateRequest dto);

    default ApiResponse<List<PostSummaryResponse>> toPagedSummary(Page<Post> page) {
        List<PostSummaryResponse> data = toSummaryList(page.getContent());
        return new ApiResponse<>(data, "OK", ApiResponse.Meta.from(page));
    }

    @Named("instantToOffsetDateTimeUtc")
    static OffsetDateTime instantToOffsetDateTimeUtc(Instant ts) {
        return ts == null ? null : ts.atOffset(ZoneOffset.UTC);
    }
}
