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

@Entity
@Table(name = "posts_like")
public class PostLike {

    @Id
    @SequenceGenerator(name = "like_seq_gen", sequenceName = "seq_like_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "like_seq_gen")
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "clienthash", length = 30, nullable = false)
    private String clientHash;

    @Column(name = "created_at", nullable = false)
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    private Instant createdAt;

    /**
     * JPA がエンティティを復元するためのコンストラクタ。
     */
    protected PostLike() {
        // JPA
    }

    /**
     * 投稿へのいいねを生成する。
     *
     * @param postId 投稿 ID
     * @param clientHash いいねしたクライアントのハッシュ
     * @param createdAt いいね日時
     */
    public PostLike(Long postId, String clientHash, Instant createdAt) {
        this.postId = postId;
        this.clientHash = clientHash;
        this.createdAt = createdAt;
    }

    /**
     * いいね ID を返す。
     *
     * @return いいね ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 投稿 ID を返す。
     *
     * @return 投稿 ID
     */
    public Long getPostId() {
        return postId;
    }

    /**
     * クライアントハッシュを返す。
     *
     * @return クライアントハッシュ
     */
    public String getClientHash() {
        return clientHash;
    }

    /**
     * いいね日時を返す。
     *
     * @return いいね日時
     */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
