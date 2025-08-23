package com.example.blog_be_springboot.helper;

import com.example.blog_be_springboot.dto.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import java.net.URI;
import java.util.List;

public final class Responses {
    private Responses() {}

    public static <T> ResponseEntity<ApiResponse<T>> ok(T body) {
        return ResponseEntity.ok(ApiResponse.of(body));
    }

    public static <T> ResponseEntity<ApiResponse<List<T>>> page(Page<T> page) {
        var meta = ApiResponse.Meta.from(page);
        return ResponseEntity.ok(ApiResponse.of(page.getContent(), meta));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(String location, T body) {
        return ResponseEntity.created(URI.create(location))
                .body(ApiResponse.of(body, "Created"));
    }

    public static ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }
}
