package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class PostLikeRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Test
    @DisplayName("いいね保存_同一投稿とclientHash_1件取得できる")
    void いいね保存_同一投稿とclientHash_1件取得できる() {
        Post post = postRepository.save(new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        postLikeRepository.save(new PostLike(post, "a1b2c3d4"));

        Optional<PostLike> like = postLikeRepository.findByPostIdAndClientHash(post.getId(), "a1b2c3d4");

        assertThat(like).isPresent();
        assertThat(like.get().getPost()).isEqualTo(post);
        assertThat(like.get().getClientHash()).isEqualTo("a1b2c3d4");
    }

    @Test
    @DisplayName("いいね保存_同一投稿とclientHash重複_DB制約で失敗する")
    void いいね保存_同一投稿とclientHash重複_DB制約で失敗する() {
        Post post = postRepository.save(new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        postLikeRepository.saveAndFlush(new PostLike(post, "a1b2c3d4"));

        assertThatThrownBy(() -> postLikeRepository.saveAndFlush(new PostLike(post, "a1b2c3d4")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("いいね検索_同一投稿とclientHashが存在する_TRUEを返す")
    void いいね検索_同一投稿とclientHashが存在する_TRUEを返す() {
        Post post = postRepository.save(new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        postLikeRepository.save(new PostLike(post, "a1b2c3d4"));

        boolean exists = postLikeRepository.existsByPostIdAndClientHash(post.getId(), "a1b2c3d4");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("いいね数取得_投稿ごとの件数を返す")
    void いいね数取得_投稿ごとの件数を返す() {
        Post target = postRepository.save(new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        Post other = postRepository.save(new Post("bob", "hi", Instant.parse("2026-05-23T10:01:00Z")));
        postLikeRepository.save(new PostLike(target, "a1b2c3d4"));
        postLikeRepository.save(new PostLike(target, "e5f6a7b8"));
        postLikeRepository.save(new PostLike(other, "a1b2c3d4"));

        long count = postLikeRepository.countByPostId(target.getId());

        assertThat(count).isEqualTo(2);
    }
}
