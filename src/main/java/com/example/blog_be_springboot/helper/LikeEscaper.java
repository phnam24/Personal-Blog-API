package com.example.blog_be_springboot.helper;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class LikeEscaper {
    public static String escape(String s) {
        // escape đơn giản cho LIKE; Hibernate sẽ truyền nguyên văn
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}