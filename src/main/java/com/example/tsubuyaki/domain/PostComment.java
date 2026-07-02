package com.example.tsubuyaki.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "post_comments")
public class PostComment {

    @Id
    @SequenceGenerator(name = "post_comments_seq_gen", sequenceName = "post_comments_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_comments_seq_gen")
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "author", length = 30, nullable = false)
    private String author;

    @Column(name = "body", length = 280, nullable = false)
    private String body;

    @Column(name = "avatar_color", length = 10, nullable = false)
    private String avatarColor;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PostComment() {
        // JPA
    }

    public PostComment(Long postId, String author, String body, String avatarColor, Instant createdAt) {
        this.postId = postId;
        this.author = author;
        this.body = body;
        this.avatarColor = avatarColor;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getPostId() {
        return postId;
    }

    public String getAuthor() {
        return author;
    }

    public String getBody() {
        return body;
    }

    public String getAvatarColor() {
        return avatarColor;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
