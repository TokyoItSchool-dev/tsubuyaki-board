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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

@Entity
@Table(name = "replies")
public class Reply {

    private static final ZoneOffset DATABASE_ZONE = ZoneOffset.UTC;

    @Id
    @SequenceGenerator(name = "replies_seq_gen", sequenceName = "replies_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "replies_seq_gen")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_reply_id")
    private Reply parent;

    @Column(name = "author", length = 15, nullable = false)
    private String author;

    @Column(name = "body", length = 1000, nullable = false)
    private String body;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    protected Reply() {
        // JPA
    }

    public Reply(Post post, Reply parent, String author, String body, Instant createdAt) {
        this.post = post;
        this.parent = parent;
        this.author = author;
        this.body = body;
        this.createdAt = toDatabaseTimestamp(createdAt);
    }

    public Long getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public Reply getParent() {
        return parent;
    }

    public Long getParentId() {
        if (parent == null) {
            return null;
        }
        return parent.getId();
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

    public Instant getReadAt() {
        return toInstant(readAt);
    }

    public boolean isRead() {
        return readAt != null;
    }

    public void markRead(Instant readAt) {
        this.readAt = toDatabaseTimestamp(readAt);
    }

    public void markUnread() {
        this.readAt = null;
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
        if (!(o instanceof Reply other)) {
            return false;
        }
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
