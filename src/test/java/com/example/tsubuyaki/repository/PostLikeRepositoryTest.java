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
    @DisplayName("いいね_初回_1件登録される")
    void save_whenFirstLike_registersOneLike() {
        Post post = savePost();

        postLikeRepository.save(new PostLike(post, "aaaaaaaa"));

        assertThat(postLikeRepository.countByPostId(post.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("いいね_同じclientHashで再度押す_いいねが解除される")
    void delete_whenSameClientHashLikesAgain_removesLike() {
        Post post = savePost();
        postLikeRepository.save(new PostLike(post, "aaaaaaaa"));

        postLikeRepository.findByPostIdAndClientHash(post.getId(), "aaaaaaaa")
                .ifPresent(postLikeRepository::delete);

        assertThat(postLikeRepository.countByPostId(post.getId())).isZero();
    }

    @Test
    @DisplayName("いいね_異なるclientHash_それぞれいいねできる")
    void save_whenDifferentClientHashes_registersEachLike() {
        Post post = savePost();

        postLikeRepository.save(new PostLike(post, "aaaaaaaa"));
        postLikeRepository.save(new PostLike(post, "bbbbbbbb"));

        assertThat(postLikeRepository.countByPostId(post.getId())).isEqualTo(2);
    }

    private Post savePost() {
        return postRepository.save(new Post("alice", "朝の共有です", Instant.parse("2026-05-23T10:00:00Z")));
    }
}
