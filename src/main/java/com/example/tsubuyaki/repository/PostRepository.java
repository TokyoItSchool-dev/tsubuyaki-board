package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = "tags")
    List<Post> findTop50ByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "tags")
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = "tags")
    List<Post> findByBodyContainingOrderByCreatedAtDesc(String body);

    @EntityGraph(attributePaths = "tags")
    Page<Post> findByBodyContainingOrderByCreatedAtDesc(String body, Pageable pageable);

    @EntityGraph(attributePaths = "tags")
    List<Post> findDistinctByTagsNameOrderByCreatedAtDesc(String tagName);

    @EntityGraph(attributePaths = "tags")
    Page<Post> findDistinctByTagsNameOrderByCreatedAtDesc(String tagName, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "tags")
    Optional<Post> findById(Long id);
}
