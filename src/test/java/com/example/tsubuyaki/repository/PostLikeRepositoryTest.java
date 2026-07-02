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
    @DisplayName("Repository_いいね_投稿idごとの件数を返す")
    void いいね_投稿idごとの件数を返す() {
        Post firstPost = postRepository.saveAndFlush(
                new Post("alice", "S1 の投稿", Instant.parse("2026-06-26T12:00:00Z")));
        Post secondPost = postRepository.saveAndFlush(
                new Post("bob", "別の投稿", Instant.parse("2026-06-26T12:01:00Z")));

        postLikeRepository.save(new PostLike(firstPost, "aaaaaaaa", Instant.parse("2026-06-26T12:02:00Z")));
        postLikeRepository.save(new PostLike(firstPost, "bbbbbbbb", Instant.parse("2026-06-26T12:03:00Z")));
        postLikeRepository.save(new PostLike(secondPost, "cccccccc", Instant.parse("2026-06-26T12:04:00Z")));
        postLikeRepository.flush();

        assertThat(postLikeRepository.countByPostId(firstPost.getId())).isEqualTo(2);
        assertThat(postLikeRepository.countByPostId(secondPost.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Repository_いいね解除_同じ投稿idとclientHashを削除できる")
    void いいね解除_同じ投稿idとclientHashを削除できる() {
        Post post = postRepository.saveAndFlush(
                new Post("alice", "S1 の投稿", Instant.parse("2026-06-26T12:00:00Z")));
        postLikeRepository.saveAndFlush(
                new PostLike(post, "aaaaaaaa", Instant.parse("2026-06-26T12:01:00Z")));

        postLikeRepository.deleteByPostIdAndClientHash(post.getId(), "aaaaaaaa");
        postLikeRepository.flush();

        assertThat(postLikeRepository.existsByPostIdAndClientHash(post.getId(), "aaaaaaaa")).isFalse();
        assertThat(postLikeRepository.countByPostId(post.getId())).isZero();
    }
}
