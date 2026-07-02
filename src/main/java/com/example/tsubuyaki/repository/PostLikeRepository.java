/*
 * 投稿へのいいね状態といいね数を取得する Spring Data JPA リポジトリ。
 */
package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    /**
     * 指定投稿に対する同一クライアントのいいね状態を取得する。
     *
     * @param postId 投稿 ID
     * @param clientHash 利用者を識別する短いハッシュ
     * @return 既にいいね済みの場合は該当レコード
     */
    Optional<PostLike> findByPostIdAndClientHash(Long postId, String clientHash);

    /**
     * 指定投稿のいいね数を数える。
     *
     * @param postId 投稿 ID
     * @return 指定投稿に紐づくいいね数
     */
    long countByPostId(Long postId);
}
