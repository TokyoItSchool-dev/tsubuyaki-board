package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostTag;
import com.example.tsubuyaki.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {

    @Query("""
            SELECT DISTINCT pt.post
            FROM PostTag pt
            LEFT JOIN FETCH pt.post.postTags postTag
            LEFT JOIN FETCH postTag.tag
            WHERE pt.tag.name = :name
              AND pt.post.deletedAt IS NULL
            ORDER BY pt.post.createdAt DESC
            """)
    List<Post> findPostsByTagName(@Param("name") String name);

    @Query("""
            SELECT pt.tag
            FROM PostTag pt
            WHERE pt.post.id = :postId
            ORDER BY pt.tag.name ASC
            """)
    List<Tag> findTagsByPostId(@Param("postId") Long postId);

    @Modifying
    void deleteByPostId(Long postId);
}
