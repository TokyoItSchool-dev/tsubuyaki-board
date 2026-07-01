package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

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
    @DisplayName("いいね数_投稿別に保存済みいいね数をカウントする")
    void いいね数_投稿別に保存済みいいね数をカウントする() {
        Post post = postRepository.save(new Post("alice", "本文です",
                LocalDateTime.parse("2026-05-23T10:00:00")));
        Post otherPost = postRepository.save(new Post("bob", "別投稿です",
                LocalDateTime.parse("2026-05-23T10:01:00")));

        postLikeRepository.save(new PostLike(post.getId(), "client01"));
        postLikeRepository.save(new PostLike(post.getId(), "client02"));
        postLikeRepository.save(new PostLike(otherPost.getId(), "client01"));

        assertThat(postLikeRepository.countByPostId(post.getId())).isEqualTo(2L);
        assertThat(postLikeRepository.countByPostId(otherPost.getId())).isEqualTo(1L);
    }
}
