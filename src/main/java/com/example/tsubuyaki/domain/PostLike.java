package com.example.tsubuyaki.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "post_likes")
public class PostLike {

    @EmbeddedId
    private PostLikeId id;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PostLike() {
        // JPA
    }

    public PostLike(PostLikeId id, Instant createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public PostLikeId getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
