package com.example.tsubuyaki.domain;

import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
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
@Table(name = "posts")
public class Post {

    @Id
    @SequenceGenerator(name = "posts_seq_gen", sequenceName = "posts_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "posts_seq_gen")
    private Long id;

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "body", length = 280, nullable = false)
    private String body;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Post() {
        // JPA
    }

    public Post(String author, String body, Instant createdAt) {
        this(new User(author, User.DEFAULT_AVATAR_COLOR), body, createdAt);
    }

    public Post(String author, String body, Instant createdAt, String avatarColor) {
        this(new User(author, avatarColor), body, createdAt);
    }

    public Post(User user, String body, Instant createdAt) {
        this.user = user;
        this.body = body;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getAuthor() {
        return user.getName();
    }

    public String getBody() {
        return body;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getAvatarColor() {
        return user.getAvatarColor();
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

    public User getUser() {
        return user;
    }
}
