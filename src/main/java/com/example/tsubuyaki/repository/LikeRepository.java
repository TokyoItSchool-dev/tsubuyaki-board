package com.example.tsubuyaki.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<LikeEntity, Long> {

    boolean existsByPostIdAndClientHash(Long postId, String clientHash);

    Optional<LikeEntity> findByPostIdAndClientHash(Long postId, String clientHash);

    long countByPostId(Long postId);
}
