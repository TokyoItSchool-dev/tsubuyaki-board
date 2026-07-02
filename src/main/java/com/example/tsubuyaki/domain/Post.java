package com.example.tsubuyaki.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.jdbc.TimestampJdbcType;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

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

    @Column(name = "avatar_color", length = 20, nullable = false)
    private String avatarColor;

    @JdbcType(TimestampJdbcType.class)
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP(6)")
    private LocalDateTime createdAt;

    @ManyToMany
    @JoinTable(
            name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new LinkedHashSet<>();

    protected Post() {
        // JPA
    }

    public Post(String author, String body, LocalDateTime createdAt) {
        this(author, body, createdAt, "gray");
    }

    public Post(String author, String body, LocalDateTime createdAt, String avatarColor) {
        this.author = author;
        this.body = body;
        this.createdAt = createdAt;
        this.avatarColor = avatarColor == null || avatarColor.isBlank() ? "gray" : avatarColor;
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

    public String getAvatarColor() {
        return avatarColor;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public void addTag(Tag tag) {
        tags.add(tag);
        tag.getPosts().add(this);
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
