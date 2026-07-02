package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.domain.PostLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeId> {

    long countByIdPostId(Long postId);

    @Query("""
            SELECT pl.id.postId AS postId, COUNT(pl) AS likeCount
            FROM PostLike pl
            WHERE pl.id.postId IN :postIds
            GROUP BY pl.id.postId
            """)
    List<PostLikeCount> findCountsByPostIdIn(@Param("postIds") Collection<Long> postIds);

    interface PostLikeCount {

        Long getPostId();

        Long getLikeCount();
    }
}
