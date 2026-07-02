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

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "post_replies")
public class PostReply {

    @Id
    @SequenceGenerator(name = "post_replies_seq_gen", sequenceName = "post_replies_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_replies_seq_gen")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "author", length = 30, nullable = false)
    private String author;

    @Column(name = "body", length = 280, nullable = false)
    private String body;

    @Column(name = "avatar_color", length = 20, nullable = false)
    private String avatarColor;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PostReply() {
        // JPA
    }

    public PostReply(Post post, String author, String body, String avatarColor) {
        this(post, author, body, Instant.now(), avatarColor);
    }

    public PostReply(Post post, String author, String body, Instant createdAt, String avatarColor) {
        this.post = post;
        this.author = author;
        this.body = body;
        this.createdAt = createdAt;
        this.avatarColor = normalizeAvatarColor(avatarColor);
    }

    public Long getId() {
        return id;
    }

    public Post getPost() {
        return post;
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

    private static String normalizeAvatarColor(String avatarColor) {
        if (avatarColor == null || avatarColor.isBlank()) {
            return Post.DEFAULT_AVATAR_COLOR;
        }
        return avatarColor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PostReply other)) {
            return false;
        }
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
