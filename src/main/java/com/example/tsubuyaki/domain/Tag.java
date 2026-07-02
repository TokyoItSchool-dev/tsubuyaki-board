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
@Table(name = "tag")
public class Tag {

    @Id
    @SequenceGenerator(name = "tag_seq_gen", sequenceName = "seq_tag", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tag_seq_gen")
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "tag_name", length = 30, nullable = false)
    private String tagName;

    @Column(name = "created_at", nullable = false)
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    private Instant createdAt;

    /**
     * JPA がエンティティを復元するためのコンストラクタ。
     */
    protected Tag() {
        // JPA
    }

    /**
     * 投稿に紐づくタグを生成する。
     *
     * @param postId 投稿 ID
     * @param tagName タグ名
     * @param createdAt 作成日時
     */
    public Tag(Long postId, String tagName, Instant createdAt) {
        this.postId = postId;
        this.tagName = tagName;
        this.createdAt = createdAt;
    }

    /**
     * タグ ID を返す。
     *
     * @return タグ ID
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
     * タグ名を返す。
     *
     * @return タグ名
     */
    public String getTagName() {
        return tagName;
    }

    /**
     * 作成日時を返す。
     *
     * @return 作成日時
     */
    public Instant getCreatedAt() {
        return createdAt;
    }
}
