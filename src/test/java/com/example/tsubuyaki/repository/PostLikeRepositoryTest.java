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
    @DisplayName("いいね_異なるclientHash_それぞれ独立していいねできる")
    void いいね_異なるclientHash_それぞれ独立していいねできる() {
        Post post = postRepository.save(new Post("alice", "本文", Instant.parse("2026-07-01T05:00:00Z")));

        postLikeRepository.save(new PostLike(post.getId(), "aaa11111", Instant.parse("2026-07-01T05:01:00Z")));
        postLikeRepository.save(new PostLike(post.getId(), "bbb22222", Instant.parse("2026-07-01T05:02:00Z")));
        postLikeRepository.flush();

        assertThat(postLikeRepository.countByPostId(post.getId())).isEqualTo(2L);
        assertThat(postLikeRepository.findByPostIdAndClientHash(post.getId(), "aaa11111")).isPresent();
        assertThat(postLikeRepository.findByPostIdAndClientHash(post.getId(), "bbb22222")).isPresent();
    }
}
