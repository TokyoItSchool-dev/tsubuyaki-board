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
    @DisplayName("いいね状態_同一clientHashのいいねが存在するとき_trueを返す")
    void いいね状態_同一clientHashのいいねが存在するとき_trueを返す() {
        Post post = postRepository.save(new Post(
                "alice",
                "いいね状態を確認する投稿",
                Instant.parse("2026-05-23T10:00:00Z")));
        postLikeRepository.save(new PostLike(post, "a1b2c3d4", Instant.parse("2026-05-23T10:01:00Z")));

        boolean actual = postLikeRepository.existsByPostIdAndClientHash(post.getId(), "a1b2c3d4");

        assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("いいね状態_同一clientHashのいいねが存在しないとき_falseを返す")
    void いいね状態_同一clientHashのいいねが存在しないとき_falseを返す() {
        Post post = postRepository.save(new Post(
                "alice",
                "未いいね状態を確認する投稿",
                Instant.parse("2026-05-23T10:00:00Z")));

        boolean actual = postLikeRepository.existsByPostIdAndClientHash(post.getId(), "a1b2c3d4");

        assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("いいね取得_同一clientHashのいいねが存在するとき_いいねを返す")
    void いいね取得_同一clientHashのいいねが存在するとき_いいねを返す() {
        Post post = postRepository.save(new Post(
                "alice",
                "いいねを取得する投稿",
                Instant.parse("2026-05-23T10:00:00Z")));
        PostLike postLike = postLikeRepository.save(new PostLike(
                post,
                "a1b2c3d4",
                Instant.parse("2026-05-23T10:01:00Z")));

        Optional<PostLike> actual = postLikeRepository.findByPostIdAndClientHash(post.getId(), "a1b2c3d4");

        assertThat(actual).contains(postLike);
    }

    @Test
    @DisplayName("いいね数_同一投稿に2件あるとき_2を返す")
    void いいね数_同一投稿に2件あるとき_2を返す() {
        Post post = postRepository.save(new Post(
                "alice",
                "いいね数を確認する投稿",
                Instant.parse("2026-05-23T10:00:00Z")));
        postLikeRepository.save(new PostLike(post, "a1b2c3d4", Instant.parse("2026-05-23T10:01:00Z")));
        postLikeRepository.save(new PostLike(post, "e5f6a7b8", Instant.parse("2026-05-23T10:02:00Z")));

        long actual = postLikeRepository.countByPostId(post.getId());

        assertThat(actual).isEqualTo(2);
    }
}
