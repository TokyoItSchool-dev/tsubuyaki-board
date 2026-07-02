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
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "post_likes",
        uniqueConstraints = @UniqueConstraint(name = "post_likes_post_client_uk",
                columnNames = { "post_id", "client_hash" }))
public class PostLike {

    // Oracle/H2 の post_likes_seq シーケンスを使っていいねIDを採番する。
    @Id
    @SequenceGenerator(name = "post_likes_seq_gen", sequenceName = "post_likes_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_likes_seq_gen")
    private Long id;

    // いいね対象の投稿。Postとは別テーブルで保持し、post_idで関連づける。
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // IPアドレス + User-Agent をSHA-256化した先頭8文字を保持する。
    @Column(name = "client_hash", length = 8, nullable = false)
    private String clientHash;

    // いいねした日時を、Oracle TIMESTAMP(6) と対応する LocalDateTime として保存する。
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected PostLike() {
        // JPA がエンティティを復元するための引数なしコンストラクタ。
    }

    public PostLike(Post post, String clientHash, LocalDateTime createdAt) {
        this.post = post;
        this.clientHash = clientHash;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public String getClientHash() {
        return clientHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PostLike other)) {
            return false;
        }
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
