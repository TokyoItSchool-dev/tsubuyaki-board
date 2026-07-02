package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 未削除の投稿を新しい順に最大 50 件取得する。
     *
     * @return 最新投稿一覧
     */
    List<Post> findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();

    /**
     * 本文にキーワードを含む未削除の投稿を新しい順に最大 50 件取得する。
     *
     * @param keyword 検索キーワード
     * @return 検索結果の投稿一覧
     */
    List<Post> findTop50ByBodyContainingAndDeletedAtIsNullOrderByCreatedAtDesc(String keyword);

    /**
     * 未削除の投稿を ID で取得する。
     *
     * @param id 投稿 ID
     * @return 投稿。存在しない、または削除済みの場合は空
     */
    Optional<Post> findByIdAndDeletedAtIsNull(Long id);

    /**
     * 指定 ID 群に含まれる未削除投稿を取得する。
     *
     * @param ids 投稿 ID 一覧
     * @return 投稿一覧
     */
    List<Post> findByIdInAndDeletedAtIsNull(List<Long> ids);
}
