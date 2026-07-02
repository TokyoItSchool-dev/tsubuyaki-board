package com.example.tsubuyaki.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Base64;
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
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "avatar_color", length = 7)
    private String avatarColor;

    @Column(name = "avatar_image_content_type", length = 100)
    private String avatarImageContentType;

    @Lob
    @Column(name = "avatar_image_data")
    private byte[] avatarImageData;

    public Post() {
        // JPA
    }

    public Post(String author, String body, Instant createdAt) {
        this.author = author;
        this.body = body;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getAvatarColor() {
        return avatarColor;
    }

    public void setAvatarColor(String avatarColor) {
        this.avatarColor = avatarColor;
    }

    public String getAvatarImageContentType() {
        return avatarImageContentType;
    }

    public void setAvatarImageContentType(String avatarImageContentType) {
        this.avatarImageContentType = avatarImageContentType;
    }

    public byte[] getAvatarImageData() {
        return avatarImageData;
    }

    public void setAvatarImageData(byte[] avatarImageData) {
        this.avatarImageData = avatarImageData;
    }

    public boolean hasAvatarImage() {
        return avatarImageContentType != null && avatarImageData != null && avatarImageData.length > 0;
    }

    public String getAvatarImageDataUri() {
        if (!hasAvatarImage()) {
            return "";
        }
        return "data:" + avatarImageContentType + ";base64," + Base64.getEncoder().encodeToString(avatarImageData);
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
