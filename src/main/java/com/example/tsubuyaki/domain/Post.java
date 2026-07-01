package com.example.tsubuyaki.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

@Entity
@Table(name = "posts")
public class Post {

    private static final ZoneOffset DATABASE_ZONE = ZoneOffset.UTC;

    @Id
    @SequenceGenerator(name = "posts_seq_gen", sequenceName = "posts_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "posts_seq_gen")
    private Long id;

    @Column(name = "author", length = 30, nullable = false)
    private String author;

    @Column(name = "body", length = 280, nullable = false)
    private String body;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    protected Post() {
        // JPA
    }

    public Post(String author, String body, Instant createdAt) {
        this.author = author;
        this.body = body;
        this.createdAt = toDatabaseTimestamp(createdAt);
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

    public Instant getCreatedAt() {
        return toInstant(createdAt);
    }

    public Instant getDeletedAt() {
        return toInstant(deletedAt);
    }

    public void markDeleted(Instant deletedAt) {
        this.deletedAt = toDatabaseTimestamp(deletedAt);
    }

    private static LocalDateTime toDatabaseTimestamp(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, DATABASE_ZONE);
    }

    private static Instant toInstant(LocalDateTime timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toInstant(DATABASE_ZONE);
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
