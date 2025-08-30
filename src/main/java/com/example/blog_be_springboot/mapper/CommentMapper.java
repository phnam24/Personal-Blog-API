package com.example.blog_be_springboot.mapper;

import com.example.blog_be_springboot.dto.request.CommentCreateRequest;
import com.example.blog_be_springboot.dto.request.CommentUpdateRequest;
import com.example.blog_be_springboot.dto.response.ApiResponse;
import com.example.blog_be_springboot.dto.response.CommentResponse;
import com.example.blog_be_springboot.dto.response.CommentTreeResponse;
import com.example.blog_be_springboot.entity.Comment;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {UserMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CommentMapper {
    @Mapping(source = "post.id", target = "postId")
    @Mapping(source = "user", target = "author") // UserMapper.toAuthorBrief
    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "instantToOffsetDateTimeUtc")
    CommentResponse toCommentResponse(Comment e);

    List<CommentResponse> toCommentResponseList(List<Comment> list);

    default ApiResponse<List<CommentResponse>> toPagedResponse(Page<Comment> page) {
        List<CommentResponse> data = toCommentResponseList(page.getContent());
        return new ApiResponse<>(data, "OK", ApiResponse.Meta.from(page));
    }

    default ApiResponse<List<CommentTreeResponse>> toPagedTreeResponse(Page<Comment> page) {
        List<CommentTreeResponse> data = toTreeList(page.getContent());
        return new ApiResponse<>(data, "OK", ApiResponse.Meta.from(page));
    }

    @Mapping(source = "post.id", target = "postId")
    @Mapping(source = "user", target = "author")
    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "instantToOffsetDateTimeUtc")
    @Mapping(source = "children", target = "children") // đệ quy
    CommentTreeResponse toTree(Comment e);

    List<CommentTreeResponse> toTreeList(List<Comment> nodes);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "post", ignore = true)     // set ở service từ path param
    @Mapping(target = "user", ignore = true)     // set ở service từ CurrentUserProvider
    @Mapping(target = "parent", ignore = true)   // set ở service nếu parentId != null
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "createdAt", ignore = true) // DB/@PrePersist
    Comment toEntity(CommentCreateRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "post", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void update(@MappingTarget Comment e, CommentUpdateRequest dto);

    @Named("instantToOffsetDateTimeUtc")
    static OffsetDateTime instantToOffsetDateTimeUtc(Instant ts) {
        return ts == null ? null : ts.atOffset(ZoneOffset.UTC);
    }
}
