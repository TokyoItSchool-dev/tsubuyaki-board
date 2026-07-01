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
    @DisplayName("いいね_保存と削除_投稿に紐づく件数が増減する")
    void saveAndDeleteLike_changesCountByPostId() {
        Post post = postRepository.save(new Post(
                "alice", "本文です", Instant.parse("2026-05-23T00:00:00Z")));
        PostLike like = postLikeRepository.saveAndFlush(new PostLike(
                post, "abcd1234", Instant.parse("2026-05-23T01:00:00Z")));

        assertThat(postLikeRepository.countByPostId(post.getId())).isEqualTo(1L);
        assertThat(postLikeRepository.findByPostIdAndClientHash(post.getId(), "abcd1234"))
                .contains(like);
        assertThat(postLikeRepository.existsByPostIdAndClientHash(post.getId(), "abcd1234"))
                .isTrue();

        postLikeRepository.delete(like);
        postLikeRepository.flush();

        assertThat(postLikeRepository.countByPostId(post.getId())).isZero();
    }

    @Test
    @DisplayName("いいね_同一投稿とclientHashの組み合わせ_一意制約で重複保存できない")
    void save_whenSamePostAndClientHash_throwsDataIntegrityViolation() {
        Post post = postRepository.save(new Post(
                "alice", "本文です", Instant.parse("2026-05-23T00:00:00Z")));
        postLikeRepository.saveAndFlush(new PostLike(
                post, "abcd1234", Instant.parse("2026-05-23T01:00:00Z")));

        assertThatThrownBy(() -> postLikeRepository.saveAndFlush(new PostLike(
                post, "abcd1234", Instant.parse("2026-05-23T02:00:00Z"))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
