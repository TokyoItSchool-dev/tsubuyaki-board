package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();

    List<Post> findTop50ByBodyContainingAndDeletedAtIsNullOrderByCreatedAtDesc(String keyword);

    long countByBodyContainingAndDeletedAtIsNull(String keyword);

    Optional<Post> findByIdAndDeletedAtIsNull(Long id);
}
