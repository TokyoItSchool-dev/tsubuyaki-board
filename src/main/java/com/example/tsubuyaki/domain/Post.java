/*
 * 投稿本文、投稿者名、任意のアバター色、作成日時を保持する投稿エンティティ。
 */
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

    @Column(name = "avatar_color", length = 20)
    private String avatarColor;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Post() {
        // JPA
    }

    public Post(String author, String body, LocalDateTime createdAt) {
        this(author, body, createdAt, null);
    }

    public Post(String author, String body, LocalDateTime createdAt, String avatarColor) {
        this.author = author;
        this.body = body;
        this.createdAt = createdAt;
        this.avatarColor = avatarColor;
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

    /**
     * 同じ投稿かどうかをエンティティ ID で判定する。
     *
     * <p>投稿本文やアバター色は後から仕様変更で変わる可能性があるため、
     * 永続化後の同一性は主キーだけで判断する。</p>
     *
     * @param o 比較対象
     * @return 同じ ID の投稿であれば {@code true}
     */
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

    /**
     * エンティティ ID をもとにハッシュ値を返す。
     *
     * <p>{@link #equals(Object)} と同じく、投稿内容ではなく主キーに基づいて
     * コレクション内の同一性を扱う。</p>
     *
     * @return ID から計算したハッシュ値
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
