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

    // 投稿保存に使うRepository。いいね対象の投稿を先にDBへ用意する。
    @Autowired
    private PostRepository postRepository;

    // いいね保存・検索・件数取得に使うRepository。
    @Autowired
    private PostLikeRepository postLikeRepository;

    @Test
    @DisplayName("いいね_初回保存_いいね数が1になる")
    void いいね_初回保存_いいね数が1になる() {
        Post post = postRepository.save(new Post("alice", "本文です", LocalDateTime.of(2026, 5, 23, 10, 0)));

        postLikeRepository.save(new PostLike(post, "abcd1234", LocalDateTime.of(2026, 5, 23, 10, 1)));

        assertThat(postLikeRepository.countByPostId(post.getId())).isEqualTo(1L);
        assertThat(postLikeRepository.findByPostIdAndClientHash(post.getId(), "abcd1234")).isPresent();
    }

    @Test
    @DisplayName("いいね_異なるclientHash_別ユーザーとして件数に加算される")
    void いいね_異なるclientHash_別ユーザーとして件数に加算される() {
        Post post = postRepository.save(new Post("alice", "本文です", LocalDateTime.of(2026, 5, 23, 10, 0)));

        postLikeRepository.save(new PostLike(post, "abcd1234", LocalDateTime.of(2026, 5, 23, 10, 1)));
        postLikeRepository.save(new PostLike(post, "bbbb2222", LocalDateTime.of(2026, 5, 23, 10, 2)));

        assertThat(postLikeRepository.countByPostId(post.getId())).isEqualTo(2L);
    }
}
