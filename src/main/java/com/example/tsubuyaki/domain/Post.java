package com.example.tsubuyaki.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "background_color", length = 7)
    private String backgroundColor;

    @Column(name = "client_hash", length = 8)
    private String clientHash;

    @Column(name = "deleted_at", nullable = false)
    private int deletedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected Post() {
        // JPA
    }

    public Post(String author, String body, LocalDateTime createdAt) {
        this(author, body, createdAt, PostBackgroundColor.DEFAULT);
    }

    public Post(String author, String body, LocalDateTime createdAt, String backgroundColor) {
        this(author, body, createdAt, backgroundColor, null);
    }

    public Post(String author, String body, LocalDateTime createdAt, String backgroundColor, String clientHash) {
        this.author = author;
        this.body = body;
        this.createdAt = createdAt;
        this.backgroundColor = backgroundColor;
        this.clientHash = clientHash;
        this.deletedAt = 0;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getBackgroundColor() {
        return PostBackgroundColor.normalize(backgroundColor);
    }

    public String getClientHash() {
        return clientHash;
    }

    public int getDeletedAt() {
        return deletedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean canDelete(String currentClientHash) {
        return canModify(currentClientHash);
    }

    public boolean canModify(String currentClientHash) {
        return clientHash != null && clientHash.equals(currentClientHash);
    }

    public void markDeleted() {
        this.deletedAt = 1;
    }

    public void updateBodyAndBackgroundColor(String body, String backgroundColor, LocalDateTime updatedAt) {
        this.body = body;
        this.backgroundColor = backgroundColor;
        this.updatedAt = updatedAt;
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
