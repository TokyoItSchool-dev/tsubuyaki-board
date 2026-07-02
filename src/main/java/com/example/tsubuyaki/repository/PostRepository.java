package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();

    List<Post> findTop50ByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc(String q);

    List<Post> findTop50ByDeletedAtIsNullAndTagsNameOrderByCreatedAtDesc(String name);

    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.tags WHERE p.id IN :ids")
    List<Post> findAllWithTags(@Param("ids") List<Long> ids);

    Optional<Post> findByIdAndDeletedAtIsNull(Long id);
}
