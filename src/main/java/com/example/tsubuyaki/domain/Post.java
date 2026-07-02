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
import java.util.Objects;

@Entity
@Table(name = "posts")
public class Post {

    public static final String DEFAULT_COLOR = "E0F2FE";
    public static final String DEFAULT_CLIENT_HASH = "legacy";

    @Id
    @SequenceGenerator(name = "posts_seq_gen", sequenceName = "posts_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "posts_seq_gen")
    private Long id;

    @Column(name = "author", length = 30, nullable = false)
    private String author;

    @Column(name = "body", length = 280, nullable = false)
    private String body;

    @Column(name = "created_at", nullable = false)
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    private Instant createdAt;

    @Column(name = "color", length = 6, nullable = false)
    private String color;

    @Column(name = "clienthash", length = 30, nullable = false)
    private String clientHash;

    @Column(name = "deleted_at")
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    private Instant deletedAt;

    /**
     * JPA がエンティティを復元するためのコンストラクタ。
     */
    protected Post() {
        // JPA
    }

    /**
     * デフォルト色で投稿を生成する。
     *
     * @param author 投稿者名
     * @param body 本文
     * @param createdAt 投稿日時
     */
    public Post(String author, String body, Instant createdAt) {
        this(author, body, createdAt, DEFAULT_COLOR);
    }

    /**
     * 背景色を指定して投稿を生成する。
     *
     * @param author 投稿者名
     * @param body 本文
     * @param createdAt 投稿日時
     * @param color 背景色コード
     */
    public Post(String author, String body, Instant createdAt, String color) {
        this(author, body, createdAt, color, DEFAULT_CLIENT_HASH);
    }

    /**
     * 背景色とクライアントハッシュを指定して投稿を生成する。
     *
     * @param author 投稿者名
     * @param body 本文
     * @param createdAt 投稿日時
     * @param color 背景色コード
     * @param clientHash 投稿者判定に使うクライアントハッシュ
     */
    public Post(String author, String body, Instant createdAt, String color, String clientHash) {
        this.author = author;
        this.body = body;
        this.createdAt = createdAt;
        this.color = color;
        this.clientHash = clientHash;
    }

    /**
     * 投稿 ID を返す。
     *
     * @return 投稿 ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 投稿者名を返す。
     *
     * @return 投稿者名
     */
    public String getAuthor() {
        return author;
    }

    /**
     * 本文を返す。
     *
     * @return 本文
     */
    public String getBody() {
        return body;
    }

    /**
     * 投稿日時を返す。
     *
     * @return 投稿日時
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * 背景色コードを返す。
     *
     * @return 背景色コード
     */
    public String getColor() {
        return color;
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
     * 削除日時を返す。
     *
     * @return 削除日時。未削除の場合は null
     */
    public Instant getDeletedAt() {
        return deletedAt;
    }

    /**
     * 投稿を論理削除状態にする。
     *
     * @param deletedAt 削除日時
     */
    public void markDeleted(Instant deletedAt) {
        // 物理削除せず deleted_at を設定して一覧や詳細から除外する。
        this.deletedAt = deletedAt;
    }

    /**
     * 投稿 ID を基準に同一投稿か判定する。
     *
     * @param o 比較対象
     * @return 同一投稿の場合 true
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
     * 投稿 ID を基準にハッシュ値を返す。
     *
     * @return ハッシュ値
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
