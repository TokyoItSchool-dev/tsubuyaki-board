package com.example.tsubuyaki.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "posts")
public class PostEntity {

    @Id
    @SequenceGenerator(name = "posts_seq_gen", sequenceName = "posts_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "posts_seq_gen")
    private Long id;

    @Column(name = "author", length = 30, nullable = false)
    private String author;

    @Column(name = "avatar_color", length = 20, nullable = false)
    private String avatarColor;

    @Column(name = "body", length = 280, nullable = false)
    private String body;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @ManyToMany
    @JoinTable(
            name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @OrderBy("name ASC")
    private Set<TagEntity> tags = new LinkedHashSet<>();

    protected PostEntity() {
        // JPA
    }

    public PostEntity(String author, String avatarColor, String body, Instant createdAt) {
        this(null, author, avatarColor, body, createdAt);
    }

    public PostEntity(Long id, String author, String avatarColor, String body, Instant createdAt) {
        this(id, author, avatarColor, body, createdAt, List.of());
    }

    public PostEntity(
            Long id,
            String author,
            String avatarColor,
            String body,
            Instant createdAt,
            List<TagEntity> tags) {
        this(id, author, avatarColor, body, createdAt, tags, null);
    }

    public PostEntity(
            Long id,
            String author,
            String avatarColor,
            String body,
            Instant createdAt,
            List<TagEntity> tags,
            Instant deletedAt) {
        this.id = id;
        this.author = author;
        this.avatarColor = avatarColor;
        this.body = body;
        this.createdAt = createdAt;
        this.tags = new LinkedHashSet<>(tags);
        this.deletedAt = deletedAt;
    }

    public Long getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getAvatarColor() {
        return avatarColor;
    }

    public String getBody() {
        return body;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<TagEntity> getTags() {
        return List.copyOf(tags);
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void markDeleted(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PostEntity other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
