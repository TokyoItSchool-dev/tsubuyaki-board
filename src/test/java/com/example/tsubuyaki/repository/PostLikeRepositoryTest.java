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
    @DisplayName("いいねRepository_同一投稿のいいね数を数えられる")
    void countByPostId_countsLikesForPost() {
        Post post = postRepository.save(new Post("alice", "本文", Instant.parse("2026-05-23T10:00:00Z")));
        postLikeRepository.save(new PostLike(post.getId(), "abcd1234", Instant.parse("2026-05-23T10:01:00Z")));
        postLikeRepository.save(new PostLike(post.getId(), "efgh5678", Instant.parse("2026-05-23T10:02:00Z")));

        long count = postLikeRepository.countByPostId(post.getId());

        assertThat(count).isEqualTo(2);
        assertThat(postLikeRepository.findByPostIdAndClientHash(post.getId(), "abcd1234")).isPresent();
    }
}
