package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    @Query("SELECT COUNT(l) FROM PostLike l WHERE l.post.id = :postId")
    long countByPostId(@Param("postId") Long postId);

    @Query("""
            SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END
            FROM PostLike l
            WHERE l.post.id = :postId AND l.clientHash = :clientHash
            """)
    boolean existsByPostIdAndClientHash(
            @Param("postId") Long postId,
            @Param("clientHash") String clientHash);

    @Query("""
            SELECT l
            FROM PostLike l
            WHERE l.post.id = :postId AND l.clientHash = :clientHash
            """)
    Optional<PostLike> findByPostIdAndClientHash(
            @Param("postId") Long postId,
            @Param("clientHash") String clientHash);
}
