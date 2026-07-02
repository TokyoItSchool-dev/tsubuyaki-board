package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    @Query("""
            SELECT DISTINCT p
            FROM Post p
            JOIN p.tags t
            LEFT JOIN FETCH p.tags
            WHERE t.name = :name
              AND p.deletedAt IS NULL
            ORDER BY p.createdAt DESC
            """)
    List<Post> findPostsByNameOrderByPostCreatedAtDesc(@Param("name") String name);
}
