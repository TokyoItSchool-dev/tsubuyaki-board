package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class PostLikeRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Test
    @DisplayName("いいね_投稿IDとclientHash_存在確認できる")
    void いいね_投稿IDとclientHash_存在確認できる() {
        Post post = postRepository.save(new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        postLikeRepository.save(new PostLike(post.getId(), "abcdef12", Instant.parse("2026-05-23T11:00:00Z")));

        boolean exists = postLikeRepository.existsByPostIdAndClientHash(post.getId(), "abcdef12");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("いいね_投稿ID_件数を数えられる")
    void いいね_投稿ID_件数を数えられる() {
        Post post = postRepository.save(new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        postLikeRepository.save(new PostLike(post.getId(), "abcdef12", Instant.parse("2026-05-23T11:00:00Z")));
        postLikeRepository.save(new PostLike(post.getId(), "34567890", Instant.parse("2026-05-23T12:00:00Z")));

        long likeCount = postLikeRepository.countByPostId(post.getId());

        assertThat(likeCount).isEqualTo(2);
    }

    @Test
    @DisplayName("いいね_投稿IDとclientHash_削除できる")
    void いいね_投稿IDとclientHash_削除できる() {
        Post post = postRepository.save(new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        postLikeRepository.save(new PostLike(post.getId(), "abcdef12", Instant.parse("2026-05-23T11:00:00Z")));

        postLikeRepository.deleteByPostIdAndClientHash(post.getId(), "abcdef12");

        assertThat(postLikeRepository.existsByPostIdAndClientHash(post.getId(), "abcdef12")).isFalse();
    }
}
