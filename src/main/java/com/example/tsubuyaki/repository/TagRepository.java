package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * 投稿 ID に紐づくタグ一覧を取得する。
     *
     * @param postId 投稿 ID
     * @return タグ一覧
     */
    List<Tag> findByPostId(Long postId);

    /**
     * 複数投稿 ID に紐づくタグを作成日時、ID の昇順で取得する。
     *
     * @param postIds 投稿 ID 一覧
     * @return タグ一覧
     */
    List<Tag> findByPostIdInOrderByCreatedAtAscIdAsc(List<Long> postIds);

    /**
     * タグ名に一致する投稿 ID を投稿日時の降順で取得する。
     *
     * @param tagName # 付きのタグ名
     * @return 投稿 ID 一覧
     */
    @Query("""
            SELECT t.postId
            FROM Tag t, Post p
            WHERE t.postId = p.id
              AND t.tagName = :tagName
            ORDER BY p.createdAt DESC
            """)
    List<Long> findPostIdsByTagNameOrderByPostCreatedAtDesc(@Param("tagName") String tagName);
}
