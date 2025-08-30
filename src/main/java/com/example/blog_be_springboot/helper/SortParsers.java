package com.example.blog_be_springboot.helper;

import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

import java.util.Map;

@NoArgsConstructor
public final class SortParsers {
    private static final Map<String, String> ALLOWED = Map.of(
            "id", "id",
            "title", "title",
            "created_at", "createdAt",
            "updated_at", "updatedAt"
    );

    public static Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String[] parts = sortParam.split(",", 2);
        String field = parts[0].trim().toLowerCase();
        String dir = (parts.length > 1 ? parts[1].trim().toLowerCase() : "asc");

        String mapped = ALLOWED.get(field);
        if (mapped == null) {
            // bỏ qua field lạ: fallback safe
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        Sort.Direction direction = "desc".equals(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, mapped);
    }
}