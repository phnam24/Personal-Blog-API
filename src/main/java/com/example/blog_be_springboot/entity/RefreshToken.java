package com.example.blog_be_springboot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_user", columnList = "user_id"),
        @Index(name = "idx_refresh_expires", columnList = "expiresAt")
})
public class RefreshToken {
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;           // jti từ refresh JWT

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_refresh_user"))
    private User user;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    @Column(name = "replaced_by", columnDefinition = "BINARY(16)")
    private UUID replacedBy; // jti mới khi rotate

    private Instant createdAt = Instant.now();
    private Instant lastUsedAt;
}
