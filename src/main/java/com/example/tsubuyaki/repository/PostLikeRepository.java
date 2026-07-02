package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    long countByPostId(Long postId);

    @Query("""
            SELECT new com.example.tsubuyaki.repository.PostLikeCount(pl.post.id, COUNT(pl.id))
            FROM PostLike pl
            WHERE pl.post.id IN :postIds
            GROUP BY pl.post.id
            """)
    List<PostLikeCount> countByPostIds(@Param("postIds") List<Long> postIds);

}
