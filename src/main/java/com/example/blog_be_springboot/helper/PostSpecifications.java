package com.example.blog_be_springboot.helper;

import com.example.blog_be_springboot.entity.Post;
import jakarta.persistence.criteria.JoinType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostSpecifications {

    public static Specification<Post> build(
            String q,
            Long authorId,
            Long tagId,
            Instant createdFrom, Instant createdTo,
            Instant updatedFrom, Instant updatedTo
    ) {
        return Specification
                .allOf(
                        fullTextLike(q),
                        byAuthorId(authorId),
                        byTagId(tagId),
                        createdBetween(createdFrom, createdTo),
                        updatedBetween(updatedFrom, updatedTo),
                        distinctIfJoin(tagId != null));
    }

    /* q: LIKE title OR content (lowercase, escape wildcard) */
    public static Specification<Post> fullTextLike(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) return null;
            String pattern = "%" + LikeEscaper.escape(q.toLowerCase()) + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("content")), pattern)
            );
        };
    }

    public static Specification<Post> byAuthorId(Long authorId) {
        return (root, query, cb) -> {
            if (authorId == null) return null;
            return cb.equal(root.get("author").get("id"), authorId);
        };
    }

    public static Specification<Post> byTagId(Long tagId) {
        return (root, query, cb) -> {
            if (tagId == null) return null;
            var tags = root.join("tags", JoinType.INNER);
            assert query != null;
            query.distinct(true);
            return cb.equal(tags.get("id"), tagId);
        };
    }

    public static Specification<Post> createdBetween(Instant from, Instant to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from != null && to != null) return cb.between(root.get("createdAt"), from, to);
            return (from != null)
                    ? cb.greaterThanOrEqualTo(root.get("createdAt"), from)
                    : cb.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }

    public static Specification<Post> updatedBetween(Instant from, Instant to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from != null && to != null) return cb.between(root.get("updatedAt"), from, to);
            return (from != null)
                    ? cb.greaterThanOrEqualTo(root.get("updatedAt"), from)
                    : cb.lessThanOrEqualTo(root.get("updatedAt"), to);
        };
    }

    /** đảm bảo distinct khi có join */
    private static Specification<Post> distinctIfJoin(boolean needDistinct) {
        return (root, query, cb) -> {
            if (needDistinct) query.distinct(true);
            return null;
        };
    }
}
