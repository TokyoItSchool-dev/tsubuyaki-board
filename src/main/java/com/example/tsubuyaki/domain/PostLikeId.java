package com.example.tsubuyaki.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PostLikeId implements Serializable {

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "client_hash", length = 8, nullable = false)
    private String clientHash;

    protected PostLikeId() {
        // JPA
    }

    public PostLikeId(Long postId, String clientHash) {
        this.postId = postId;
        this.clientHash = clientHash;
    }

    public Long getPostId() {
        return postId;
    }

    public String getClientHash() {
        return clientHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PostLikeId other)) {
            return false;
        }
        return Objects.equals(postId, other.postId)
                && Objects.equals(clientHash, other.clientHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, clientHash);
    }
}
