package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.domain.PostLikeId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

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
    @DisplayName("いいね登録_複合主キー_投稿IDとclientHashで存在確認できる")
    void save_whenPostLikeExists_canFindByCompositeId() {
        postRepository.saveAndFlush(new Post(1L, "alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        PostLikeId id = new PostLikeId(1L, "abcd1234");

        postLikeRepository.saveAndFlush(new PostLike(id, Instant.parse("2026-05-23T11:00:00Z")));

        assertThat(postLikeRepository.existsById(id)).isTrue();
    }

    @Test
    @DisplayName("いいね数取得_投稿ID_対象投稿のいいね数を返す")
    void countByIdPostId_returnsTargetPostLikeCount() {
        Instant createdAt = Instant.parse("2026-05-23T10:00:00Z");
        postRepository.saveAndFlush(new Post(1L, "alice", "hello", createdAt));
        postRepository.saveAndFlush(new Post(2L, "bob", "hi", createdAt));
        postLikeRepository.saveAll(List.of(
                new PostLike(new PostLikeId(1L, "aaaa1111"), createdAt),
                new PostLike(new PostLikeId(1L, "bbbb2222"), createdAt),
                new PostLike(new PostLikeId(2L, "cccc3333"), createdAt)));

        long likeCount = postLikeRepository.countByIdPostId(1L);

        assertThat(likeCount).isEqualTo(2L);
    }

    @Test
    @DisplayName("いいね数一覧取得_複数投稿ID_投稿別いいね数を返す")
    void findCountsByPostIdIn_returnsLikeCountsGroupedByPostId() {
        Instant createdAt = Instant.parse("2026-05-23T10:00:00Z");
        postRepository.saveAndFlush(new Post(1L, "alice", "hello", createdAt));
        postRepository.saveAndFlush(new Post(2L, "bob", "hi", createdAt));
        postLikeRepository.saveAll(List.of(
                new PostLike(new PostLikeId(1L, "aaaa1111"), createdAt),
                new PostLike(new PostLikeId(1L, "bbbb2222"), createdAt),
                new PostLike(new PostLikeId(2L, "cccc3333"), createdAt)));

        List<PostLikeRepository.PostLikeCount> counts = postLikeRepository.findCountsByPostIdIn(List.of(1L, 2L));

        assertThat(counts)
                .extracting(PostLikeRepository.PostLikeCount::getPostId, PostLikeRepository.PostLikeCount::getLikeCount)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(1L, 2L),
                        org.assertj.core.groups.Tuple.tuple(2L, 1L));
    }
}
