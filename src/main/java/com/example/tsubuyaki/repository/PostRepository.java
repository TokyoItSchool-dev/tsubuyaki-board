package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();

    List<Post> findByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc(String keyword);

    @Query("""
            SELECT p.id
            FROM Post p
            WHERE p.deletedAt IS NULL
            ORDER BY p.createdAt DESC
            """)
    List<Long> findLatestIds(Pageable pageable);

    @Query("""
            SELECT DISTINCT p
            FROM Post p
            LEFT JOIN FETCH p.postTags pt
            LEFT JOIN FETCH pt.tag
            WHERE p.deletedAt IS NULL
              AND p.id IN :ids
            ORDER BY p.createdAt DESC
            """)
    List<Post> findAllWithTagsByIdIn(@Param("ids") List<Long> ids);

    @Query("""
            SELECT new com.example.tsubuyaki.repository.PostApiRow(
                p.id,
                p.author,
                p.body,
                p.avatarColor,
                p.createdAt,
                p.updatedAt,
                t.name,
                p.likesCount
            )
            FROM Post p
            LEFT JOIN p.postTags pt
            LEFT JOIN pt.tag t
            WHERE p.deletedAt IS NULL
              AND p.id IN :ids
            GROUP BY p.id, p.author, p.body, p.avatarColor, p.createdAt, p.updatedAt, p.likesCount, t.name
            ORDER BY p.createdAt DESC, t.name ASC
            """)
    List<PostApiRow> findApiRowsByIds(@Param("ids") List<Long> ids);

    @Query("""
            SELECT DISTINCT p
            FROM Post p
            LEFT JOIN FETCH p.postTags pt
            LEFT JOIN FETCH pt.tag
            WHERE p.deletedAt IS NULL
              AND LOCATE(:keyword, p.body) > 0
            ORDER BY p.createdAt DESC
            """)
    List<Post> findByKeywordWithTags(@Param("keyword") String keyword);

    Optional<Post> findByIdAndDeletedAtIsNull(Long id);

    @Modifying
    @Query("""
            UPDATE Post p
            SET p.likesCount = p.likesCount + 1
            WHERE p.id = :id
              AND p.deletedAt IS NULL
            """)
    int incrementLikesCountById(@Param("id") Long id);

    @Query("""
            SELECT new com.example.tsubuyaki.repository.PostLikeCount(p.id, p.likesCount)
            FROM Post p
            WHERE p.deletedAt IS NULL
              AND p.id IN :postIds
            """)
    List<PostLikeCount> findLikeCountsByPostIds(@Param("postIds") List<Long> postIds);

    @Query("""
            SELECT p.likesCount
            FROM Post p
            WHERE p.id = :id
              AND p.deletedAt IS NULL
            """)
    Optional<Long> findLikesCountById(@Param("id") Long id);
}
