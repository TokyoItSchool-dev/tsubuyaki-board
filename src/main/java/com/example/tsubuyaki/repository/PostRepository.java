/*
 * 投稿テーブルへの基本アクセスと、一覧・本文検索用クエリを定義する
 * Spring Data JPA リポジトリ。
 */
package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 一覧画面用に未削除の最新投稿を最大 50 件取得する。
     *
     * @return 作成日時の降順に並んだ投稿一覧
     */
    List<Post> findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();

    /**
     * 未削除の投稿から、本文にキーワードを含む投稿を取得する。
     *
     * @param keyword 本文検索キーワード
     * @return 作成日時の降順に並んだ検索結果
     */
    List<Post> findByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc(String keyword);
}
