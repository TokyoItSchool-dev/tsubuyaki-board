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
    @DisplayName("いいね_登録済みの場合_投稿ID単位で件数を数える")
    void いいね_登録済みの場合_投稿ID単位で件数を数える() {
        Post post = postRepository.save(new Post("alice", "本文です", Instant.parse("2026-05-23T10:00:00Z")));
        postLikeRepository.save(new PostLike(post.getId(), "abc12345", Instant.parse("2026-05-23T10:01:00Z")));
        postLikeRepository.save(new PostLike(post.getId(), "def67890", Instant.parse("2026-05-23T10:02:00Z")));

        long count = postLikeRepository.countByPostId(post.getId());

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("いいね_同一投稿同一clientHashの場合_削除できる")
    void いいね_同一投稿同一clientHashの場合_削除できる() {
        Post post = postRepository.save(new Post("alice", "本文です", Instant.parse("2026-05-23T10:00:00Z")));
        postLikeRepository.save(new PostLike(post.getId(), "abc12345", Instant.parse("2026-05-23T10:01:00Z")));

        postLikeRepository.deleteByPostIdAndClientHash(post.getId(), "abc12345");

        assertThat(postLikeRepository.existsByPostIdAndClientHash(post.getId(), "abc12345")).isFalse();
        assertThat(postLikeRepository.countByPostId(post.getId())).isZero();
    }
}
