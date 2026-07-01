package com.example.tsubuyaki.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "post_likes")
public class PostLike {

    @Id
    @SequenceGenerator(name = "post_likes_seq_gen", sequenceName = "post_likes_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_likes_seq_gen")
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "client_hash", length = 8, nullable = false)
    private String clientHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PostLike() {
        // JPA
    }

    public PostLike(Long postId, String clientHash, Instant createdAt) {
        this.postId = postId;
        this.clientHash = clientHash;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getPostId() {
        return postId;
    }

    public String getClientHash() {
        return clientHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
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
