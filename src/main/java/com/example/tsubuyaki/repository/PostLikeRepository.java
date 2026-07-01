package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    // 指定投稿に対して、同じクライアントがすでにいいね済みかを確認する。
    Optional<PostLike> findByPostIdAndClientHash(Long postId, String clientHash);

    // 投稿詳細画面に表示するため、指定投稿のいいね数を取得する。
    long countByPostId(Long postId);
}
