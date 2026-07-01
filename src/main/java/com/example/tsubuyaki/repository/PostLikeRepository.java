package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    long countByPostId(Long postId);

    boolean existsByPostIdAndClientHash(Long postId, String clientHash);

    Optional<PostLike> findByPostIdAndClientHash(Long postId, String clientHash);
}
