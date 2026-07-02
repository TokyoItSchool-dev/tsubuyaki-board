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
import java.util.Optional;

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
    @DisplayName("Repository_いいね_投稿IDとclientHashで取得できる")
    void findByPostIdAndClientHash_returnsLike() {
        Post post = postRepository.save(new Post("alice", "hello", Instant.parse("2026-06-26T09:00:00Z")));
        PostLike like = postLikeRepository.save(new PostLike(post, "facefeed",
                Instant.parse("2026-06-26T09:01:00Z")));

        Optional<PostLike> actual = postLikeRepository.findByPostIdAndClientHash(post.getId(), "facefeed");

        assertThat(actual).containsSame(like);
    }

    @Test
    @DisplayName("Repository_いいね_投稿ごとの件数を取得できる")
    void countByPostId_countsLikesForPost() {
        Post post = postRepository.save(new Post("alice", "hello", Instant.parse("2026-06-26T09:00:00Z")));
        Post otherPost = postRepository.save(new Post("bob", "good morning", Instant.parse("2026-06-26T09:02:00Z")));
        postLikeRepository.save(new PostLike(post, "facefeed", Instant.parse("2026-06-26T09:03:00Z")));
        postLikeRepository.save(new PostLike(post, "cafebabe", Instant.parse("2026-06-26T09:04:00Z")));
        postLikeRepository.save(new PostLike(otherPost, "facefeed", Instant.parse("2026-06-26T09:05:00Z")));

        long actual = postLikeRepository.countByPostId(post.getId());

        assertThat(actual).isEqualTo(2);
    }

    @Test
    @DisplayName("Repository_いいね_投稿IDとclientHashの存在確認ができる")
    void existsByPostIdAndClientHash_returnsWhetherLikeExists() {
        Post post = postRepository.save(new Post("alice", "hello", Instant.parse("2026-06-26T09:00:00Z")));
        postLikeRepository.save(new PostLike(post, "facefeed", Instant.parse("2026-06-26T09:01:00Z")));

        boolean liked = postLikeRepository.existsByPostIdAndClientHash(post.getId(), "facefeed");
        boolean notLiked = postLikeRepository.existsByPostIdAndClientHash(post.getId(), "cafebabe");

        assertThat(liked).isTrue();
        assertThat(notLiked).isFalse();
    }
}
