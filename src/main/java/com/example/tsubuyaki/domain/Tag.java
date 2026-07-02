package com.example.tsubuyaki.domain;

import java.util.Objects;

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

@Entity
@Table(name = "tags",
        uniqueConstraints = @UniqueConstraint(name = "tags_post_name_uk",
                columnNames = { "post_id", "name" }))
public class Tag {

    // Oracle/H2 の tags_seq シーケンスを使ってタグIDを採番する。
    @Id
    @SequenceGenerator(name = "tags_seq_gen", sequenceName = "tags_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tags_seq_gen")
    private Long id;

    // タグを付与した投稿。1つの投稿に複数タグを紐づけられる。
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // # を除いたタグ名を保存する。
    @Column(name = "name", length = 64, nullable = false)
    private String name;

    protected Tag() {
        // JPA がエンティティを復元するための引数なしコンストラクタ。
    }

    public Tag(Post post, String name) {
        this.post = post;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Tag other)) {
            return false;
        }
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
