package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findTop50ByOrderByCreatedAtDesc();

    List<Post> findTop50ByBodyContainingIgnoreCaseOrderByCreatedAtDesc(String keyword);

    @Query("""
            SELECT DISTINCT t.post
            FROM Tag t
            WHERE LOWER(t.name) = LOWER(:tagName)
            ORDER BY t.post.createdAt DESC
            """)
    List<Post> findByTagNameOrderByCreatedAtDesc(@Param("tagName") String tagName,
            Pageable pageable);
}
