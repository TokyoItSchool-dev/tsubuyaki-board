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
    @DisplayName("いいね_同一clientHashを検索できる")
    void findByPostIdAndClientHash_whenLikeExists_returnsLike() {
        Post post = postRepository.saveAndFlush(new Post(
                "alice", "詳細本文", Instant.parse("2026-05-23T10:00:00Z")));
        PostLike like = postLikeRepository.saveAndFlush(new PostLike(
                post, "abcd1234", Instant.parse("2026-05-23T11:00:00Z")));

        assertThat(postLikeRepository.findByPostIdAndClientHash(post.getId(), "abcd1234"))
                .contains(like);
    }

    @Test
    @DisplayName("いいね_異なるclientHash_別ユーザーのいいねとして加算する")
    void countByPostId_whenDifferentClientHashesLiked_countsEachLike() {
        Post post = postRepository.saveAndFlush(new Post(
                "alice", "詳細本文", Instant.parse("2026-05-23T10:00:00Z")));
        postLikeRepository.save(new PostLike(post, "abcd1234", Instant.parse("2026-05-23T11:00:00Z")));
        postLikeRepository.save(new PostLike(post, "efgh5678", Instant.parse("2026-05-23T11:01:00Z")));
        postLikeRepository.flush();

        assertThat(postLikeRepository.countByPostId(post.getId())).isEqualTo(2L);
    }
}
