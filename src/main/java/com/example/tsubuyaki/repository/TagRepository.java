package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByPostId(Long postId);

    List<Tag> findByPostIdInOrderByCreatedAtAscIdAsc(List<Long> postIds);

    @Query("""
            SELECT t.postId
            FROM Tag t, Post p
            WHERE t.postId = p.id
              AND t.tagName = :tagName
            ORDER BY p.createdAt DESC
            """)
    List<Long> findPostIdsByTagNameOrderByPostCreatedAtDesc(@Param("tagName") String tagName);
}
