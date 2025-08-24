package com.example.blog_be_springboot.mapper;

import com.example.blog_be_springboot.dto.response.ApiResponse;
import com.example.blog_be_springboot.dto.response.UserDetailsResponse;
import com.example.blog_be_springboot.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Mapper
public interface UserMapper {
    @Mapping(source = "createdAt", target = "creationDate", qualifiedByName = "instantToLocalDate")
    UserDetailsResponse toDetailsDto(User user);

    List<UserDetailsResponse> toDetailsDtos(List<User> users);

    @Named("instantToLocalDate")
    static LocalDate instantToLocalDate(Instant ts) {
        return ts == null ? null : ts.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    default ApiResponse<List<UserDetailsResponse>> toPagedResponse(Page<User> page) {
        List<UserDetailsResponse> data = toDetailsDtos(page.getContent());
        return new ApiResponse<>(data, "OK", ApiResponse.Meta.from(page));
    }
}
