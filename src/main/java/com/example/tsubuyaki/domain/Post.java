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

    public static final String DEFAULT_AUTHOR_COLOR = "#6b7280";

    // Oracle/H2 の posts_seq シーケンスを使って投稿IDを採番する。
    @Id
    @SequenceGenerator(name = "posts_seq_gen", sequenceName = "posts_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "posts_seq_gen")
    private Long id;

    // 投稿者名を保存する。DB側でも NOT NULL と30文字制限を持つ。
    @Column(name = "author", length = 30, nullable = false)
    private String author;

    // 投稿者の好きな色をカラーコードで保存し、一覧・詳細の投稿者アイコンに使う。
    @Column(name = "author_color", length = 7, nullable = false)
    private String authorColor = DEFAULT_AUTHOR_COLOR;

    // 投稿本文を保存する。DB側でも NOT NULL と280文字制限を持つ。
    @Column(name = "body", length = 280, nullable = false)
    private String body;

    // Oracle の TIMESTAMP(6) はタイムゾーンを持たないため、LocalDateTime として保存する。
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Post() {
        // JPA がエンティティを復元するための引数なしコンストラクタ。
    }

    public Post(String author, String body, LocalDateTime createdAt) {
        this(author, body, DEFAULT_AUTHOR_COLOR, createdAt);
    }

    public Post(String author, String body, String authorColor, LocalDateTime createdAt) {
        this.author = author;
        this.body = body;
        this.authorColor = authorColor;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getAuthorColor() {
        return authorColor;
    }

    public String getBody() {
        return body;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
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
