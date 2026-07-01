package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = "user")
    List<Post> findTop50ByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "user")
    List<Post> findTop50ByBodyContainingOrderByCreatedAtDesc(String keyword);

    @EntityGraph(attributePaths = "user")
    List<Post> findDistinctTop50ByTagsNameOrderByCreatedAtDesc(String name);

    @EntityGraph(attributePaths = { "user", "tags" })
    @Query("SELECT p FROM Post p WHERE p.id = :id")
    Optional<Post> findWithTagsById(@Param("id") Long id);

    @EntityGraph(attributePaths = "tags")
    @Query("SELECT DISTINCT p FROM Post p WHERE p.id IN :ids")
    List<Post> findAllWithTagsByIdIn(@Param("ids") Collection<Long> ids);
}
