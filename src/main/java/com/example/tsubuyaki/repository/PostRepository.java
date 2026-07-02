package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findTop50ByOrderByCreatedAtDesc();

    List<Post> findTop50ByBodyContainingOrderByCreatedAtDesc(String keyword);

    Optional<Post> findFirstByAuthorAndAvatarColorIsNotNullOrderByCreatedAtDesc(String author);

    @Modifying
    @Query("UPDATE Post p SET p.avatarColor = :avatarColor WHERE p.author = :author")
    int updateAvatarColorByAuthor(@Param("author") String author, @Param("avatarColor") String avatarColor);
}
