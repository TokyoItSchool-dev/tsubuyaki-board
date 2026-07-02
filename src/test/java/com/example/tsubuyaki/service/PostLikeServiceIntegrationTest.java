package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("h2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PostLikeServiceIntegrationTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @BeforeEach
    void setUp() {
        postLikeRepository.deleteAll();
        postRepository.deleteAll();
    }

    @Test
    @DisplayName("いいね_未いいね状態でPOST_いいねが追加される")
    void いいね_未いいね状態でPOST_いいねが追加される() {
        Post post = savePost();

        postService.toggleLike(post.getId(), "client01");

        assertThat(postService.countLikes(post.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("いいね_既にいいね済みでPOST_いいねが解除される")
    void いいね_既にいいね済みでPOST_いいねが解除される() {
        Post post = savePost();
        postService.toggleLike(post.getId(), "client01");

        postService.toggleLike(post.getId(), "client01");

        assertThat(postService.countLikes(post.getId())).isZero();
    }

    @Test
    @DisplayName("いいね_同一clientHashでPOSTを繰り返す_重複いいねは作成されない")
    void いいね_同一clientHashでPOSTを繰り返す_重複いいねは作成されない() {
        Post post = savePost();

        postService.toggleLike(post.getId(), "client01");
        assertThat(postService.countLikes(post.getId())).isEqualTo(1);

        postService.toggleLike(post.getId(), "client01");
        assertThat(postService.countLikes(post.getId())).isZero();

        postService.toggleLike(post.getId(), "client01");
        assertThat(postService.countLikes(post.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("いいね_異なるclientHash_独立していいねできる")
    void いいね_異なるclientHash_独立していいねできる() {
        Post post = savePost();

        postService.toggleLike(post.getId(), "client01");
        postService.toggleLike(post.getId(), "client02");

        assertThat(postService.countLikes(post.getId())).isEqualTo(2);

        postService.toggleLike(post.getId(), "client01");

        assertThat(postService.countLikes(post.getId())).isEqualTo(1);
    }

    private Post savePost() {
        return postRepository.save(new Post(
                "tanaka",
                "いいね対象の本文です",
                LocalDateTime.parse("2026-05-23T09:00:00")));
    }
}
