package com.example.tsubuyaki.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "posts")
public class Post {

    public static final String DEFAULT_COLOR = "E0F2FE";
    public static final String DEFAULT_CLIENT_HASH = "legacy";

    @Id
    @SequenceGenerator(name = "posts_seq_gen", sequenceName = "posts_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "posts_seq_gen")
    private Long id;

    @Column(name = "author", length = 30, nullable = false)
    private String author;

    @Column(name = "body", length = 280, nullable = false)
    private String body;

    @Column(name = "created_at", nullable = false)
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    private Instant createdAt;

    @Column(name = "color", length = 6, nullable = false)
    private String color;

    @Column(name = "clienthash", length = 30, nullable = false)
    private String clientHash;

    @Column(name = "deleted_at")
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    private Instant deletedAt;

    protected Post() {
        // JPA
    }

    public Post(String author, String body, Instant createdAt) {
        this(author, body, createdAt, DEFAULT_COLOR);
    }

    public Post(String author, String body, Instant createdAt, String color) {
        this(author, body, createdAt, color, DEFAULT_CLIENT_HASH);
    }

    public Post(String author, String body, Instant createdAt, String color, String clientHash) {
        this.author = author;
        this.body = body;
        this.createdAt = createdAt;
        this.color = color;
        this.clientHash = clientHash;
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
        return createdAt;
    }

    public String getColor() {
        return color;
    }

    public String getClientHash() {
        return clientHash;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void markDeleted(Instant deletedAt) {
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
