package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    // 同じ投稿に対する同じclientHashの既存いいねを探し、トグル時の解除判定に使う。
    Optional<PostLike> findByPostIdAndClientHash(Long postId, String clientHash);

    // 詳細画面とテストで、投稿ごとの現在のいいね総数を取得する。
    long countByPostId(Long postId);

    // clientHashが期待どおり永続化されたかを、テストから検証しやすくする。
    boolean existsByPostIdAndClientHash(Long postId, String clientHash);
}
