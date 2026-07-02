package com.example.tsubuyaki.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class LikeRepositoryTest {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("いいね保存_save_投稿IDとclientHashで存在確認できる")
    void いいね保存_save_投稿IDとclientHashで存在確認できる() {
        PostEntity post = persistPost();

        likeRepository.saveAndFlush(new LikeEntity(post.getId(), "abc12345"));
        entityManager.clear();

        assertThat(likeRepository.existsByPostIdAndClientHash(post.getId(), "abc12345")).isTrue();
        assertThat(likeRepository.findByPostIdAndClientHash(post.getId(), "abc12345")).isPresent();
    }

    @Test
    @DisplayName("いいね件数_countByPostId_投稿ごとの件数を返す")
    void いいね件数_countByPostId_投稿ごとの件数を返す() {
        PostEntity target = persistPost();
        PostEntity other = entityManager.persistAndFlush(new PostEntity(
                "bob",
                "BLUE",
                "別投稿",
                Instant.parse("2026-06-26T10:00:00Z")));
        likeRepository.save(new LikeEntity(target.getId(), "abc12345"));
        likeRepository.save(new LikeEntity(target.getId(), "def67890"));
        likeRepository.save(new LikeEntity(other.getId(), "abc12345"));
        likeRepository.flush();
        entityManager.clear();

        assertThat(likeRepository.countByPostId(target.getId())).isEqualTo(2L);
        assertThat(likeRepository.countByPostId(other.getId())).isEqualTo(1L);
    }

    @Test
    @DisplayName("いいね削除_delete_件数が減る")
    void いいね削除_delete_件数が減る() {
        PostEntity post = persistPost();
        LikeEntity like = likeRepository.saveAndFlush(new LikeEntity(post.getId(), "abc12345"));
        entityManager.clear();

        likeRepository.delete(like);
        likeRepository.flush();
        entityManager.clear();

        assertThat(likeRepository.countByPostId(post.getId())).isZero();
    }

    private PostEntity persistPost() {
        PostEntity post = new PostEntity("alice", "BLUE", "本文", Instant.parse("2026-06-26T09:00:00Z"));
        return entityManager.persistAndFlush(post);
    }
}
