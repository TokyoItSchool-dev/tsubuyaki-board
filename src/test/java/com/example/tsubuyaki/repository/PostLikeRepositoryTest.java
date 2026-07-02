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

import java.time.LocalDateTime;

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
    @DisplayName("いいね_未いいねの場合_保存できる")
    void save_whenNotLiked_savesPostLike() {
        Post post = postRepository.save(new Post("alice", "本文", LocalDateTime.parse("2026-05-23T10:00:00")));

        PostLike saved = postLikeRepository.save(new PostLike(post, "a1b2c3d4", LocalDateTime.now()));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPost()).isEqualTo(post);
        assertThat(saved.getClientHash()).isEqualTo("a1b2c3d4");
    }

    @Test
    @DisplayName("いいね数_投稿idで件数を返す")
    void countByPostId_returnsLikeCountOfPost() {
        Post target = postRepository.save(new Post("alice", "対象", LocalDateTime.parse("2026-05-23T10:00:00")));
        Post other = postRepository.save(new Post("bob", "別投稿", LocalDateTime.parse("2026-05-23T10:01:00")));
        postLikeRepository.save(new PostLike(target, "a1b2c3d4", LocalDateTime.now()));
        postLikeRepository.save(new PostLike(target, "e5f6a7b8", LocalDateTime.now()));
        postLikeRepository.save(new PostLike(other, "a1b2c3d4", LocalDateTime.now()));

        long actual = postLikeRepository.countByPostId(target.getId());

        assertThat(actual).isEqualTo(2);
    }

    @Test
    @DisplayName("いいね検索_投稿idとclientHashで存在するいいねを返す")
    void findByPostIdAndClientHash_whenLiked_returnsPostLike() {
        Post post = postRepository.save(new Post("alice", "本文", LocalDateTime.parse("2026-05-23T10:00:00")));
        PostLike like = postLikeRepository.save(new PostLike(post, "a1b2c3d4", LocalDateTime.now()));

        assertThat(postLikeRepository.findByPostIdAndClientHash(post.getId(), "a1b2c3d4"))
                .contains(like);
    }

    @Test
    @DisplayName("いいね_同一投稿同一clientHashの場合_重複登録できない")
    void save_whenSamePostAndClientHash_throwsDataIntegrityViolationException() {
        Post post = postRepository.save(new Post("alice", "本文", LocalDateTime.parse("2026-05-23T10:00:00")));
        postLikeRepository.saveAndFlush(new PostLike(post, "a1b2c3d4", LocalDateTime.now()));

        assertThatThrownBy(() -> postLikeRepository.saveAndFlush(
                new PostLike(post, "a1b2c3d4", LocalDateTime.now())))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
