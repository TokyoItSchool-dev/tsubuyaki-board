package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByPostIdAndClientHash(Long postId, String clientHash);

    long countByPostId(Long postId);

    @Query("""
            select postLike.post.id as postId, count(postLike.id) as likeCount
            from PostLike postLike
            where postLike.post.id in :postIds
            group by postLike.post.id
            """)
    List<PostLikeCount> countByPostIdIn(@Param("postIds") Collection<Long> postIds);

    interface PostLikeCount {

        Long getPostId();

        long getLikeCount();
    }
}
