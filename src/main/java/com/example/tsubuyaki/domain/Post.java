package com.example.tsubuyaki.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @SequenceGenerator(name = "posts_seq_gen", sequenceName = "posts_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "posts_seq_gen")
    private Long id;

    @Column(name = "author", length = 30, nullable = false)
    private String author;

    @Column(name = "body", length = 280, nullable = false)
    private String body;

    @Column(name = "author_icon_color", length = 7, nullable = false)
    private String authorIconColor;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Transient
    private long likeCount;

    @Transient
    private boolean liked;

    protected Post() {
        // JPA
    }

    public Post(String author, String body, Instant createdAt) {
        this(author, body, "#2563EB", createdAt);
    }

    public Post(String author, String body, String authorIconColor, Instant createdAt) {
        this.author = author;
        this.body = body;
        this.authorIconColor = authorIconColor;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getBody() {
        return body;
    }

    public String getAuthorIconColor() {
        return authorIconColor;
    }

    public String getAuthorInitial() {
        if (author == null || author.isEmpty()) {
            return "";
        }
        return author.substring(0, author.offsetByCodePoints(0, 1));
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public boolean isLiked() {
        return liked;
    }

    public void applyLikeState(long likeCount, boolean liked) {
        this.likeCount = likeCount;
        this.liked = liked;
    }

    public void delete(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Post other)) {
            return false;
        }
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
