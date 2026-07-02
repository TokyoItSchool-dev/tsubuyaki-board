package com.example.tsubuyaki.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

@Entity
@Table(name = "post_likes",
        uniqueConstraints = @UniqueConstraint(name = "post_likes_post_client_uk",
                columnNames = { "post_id", "client_hash" }))
public class PostLike {

    private static final ZoneOffset DATABASE_ZONE = ZoneOffset.UTC;

    @Id
    @SequenceGenerator(name = "post_likes_seq_gen", sequenceName = "post_likes_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_likes_seq_gen")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "client_hash", length = 8, nullable = false)
    private String clientHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected PostLike() {
        // JPA
    }

    public PostLike(Post post, String clientHash, Instant createdAt) {
        this.post = post;
        this.clientHash = clientHash;
        this.createdAt = toDatabaseTimestamp(createdAt);
    }

    public Long getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public String getClientHash() {
        return clientHash;
    }

    public Instant getCreatedAt() {
        if (createdAt == null) {
            return null;
        }
        return createdAt.toInstant(DATABASE_ZONE);
    }

    private static LocalDateTime toDatabaseTimestamp(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, DATABASE_ZONE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PostLike other)) {
            return false;
        }
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
