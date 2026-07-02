package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    /**
     * 指定投稿に対する同一クライアントのいいねが存在するか判定する。
     *
     * @param postId 投稿 ID
     * @param clientHash クライアントハッシュ
     * @return 存在する場合 true
     */
    boolean existsByPostIdAndClientHash(Long postId, String clientHash);

    /**
     * 指定投稿に対する同一クライアントのいいねを削除する。
     *
     * @param postId 投稿 ID
     * @param clientHash クライアントハッシュ
     */
    void deleteByPostIdAndClientHash(Long postId, String clientHash);

    /**
     * 指定投稿のいいね数を集計する。
     *
     * @param postId 投稿 ID
     * @return いいね数
     */
    long countByPostId(Long postId);
}
