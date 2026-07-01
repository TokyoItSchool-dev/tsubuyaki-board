package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findTop50ByOrderByCreatedAtDesc();

    @Query("SELECT COALESCE(MAX(p.id), 0) FROM Post p")
    Long findMaxId();
}
