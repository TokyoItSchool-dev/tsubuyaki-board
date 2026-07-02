package com.example.tsubuyaki.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "likes", uniqueConstraints = {
        @UniqueConstraint(name = "likes_post_client_uk", columnNames = {"post_id", "client_hash"})
})
public class LikeEntity {

    @Id
    @SequenceGenerator(name = "likes_seq_gen", sequenceName = "likes_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "likes_seq_gen")
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "client_hash", length = 8, nullable = false)
    private String clientHash;

    protected LikeEntity() {
        // JPA
    }

    public LikeEntity(Long postId, String clientHash) {
        this.postId = postId;
        this.clientHash = clientHash;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LikeEntity other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
