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
    @DisplayName("いいね_保存と削除_投稿に紐づく件数が増減する")
    void saveAndDeleteLike_changesCountByPostId() {
        Post post = postRepository.save(new Post(
                "alice", "本文です", Instant.parse("2026-05-23T00:00:00Z")));
        PostLike like = postLikeRepository.saveAndFlush(new PostLike(
                post, "abcd1234", Instant.parse("2026-05-23T01:00:00Z")));

        assertThat(postLikeRepository.countByPostId(post.getId())).isEqualTo(1L);
        assertThat(like.getId()).isNotNull();

        postLikeRepository.delete(like);
        postLikeRepository.flush();

        assertThat(postLikeRepository.countByPostId(post.getId())).isZero();
    }

    @Test
    @DisplayName("いいね一覧_投稿idごとに件数をまとめて取得する")
    void countByPostIds_returnsCountsGroupedByPostId() {
        Post post1 = postRepository.save(new Post(
                "alice", "本文1です", Instant.parse("2026-05-23T00:00:00Z")));
        Post post2 = postRepository.save(new Post(
                "bob", "本文2です", Instant.parse("2026-05-23T01:00:00Z")));
        postLikeRepository.saveAll(List.of(
                new PostLike(post1, "abcd1234", Instant.parse("2026-05-23T02:00:00Z")),
                new PostLike(post1, "efgh5678", Instant.parse("2026-05-23T03:00:00Z")),
                new PostLike(post2, "ijkl9012", Instant.parse("2026-05-23T04:00:00Z"))
        ));

        List<PostLikeCount> counts = postLikeRepository.countByPostIds(List.of(post1.getId(), post2.getId()));

        assertThat(counts)
                .extracting(PostLikeCount::postId, PostLikeCount::likesCount)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(post1.getId(), 2L),
                        org.assertj.core.groups.Tuple.tuple(post2.getId(), 1L)
                );
    }

    @DisplayName("いいね累積_同一投稿とclientHashの組み合わせ_重複保存できる")
    void save_whenSamePostAndClientHash_allowsDuplicateLikes() {
        Post post = postRepository.save(new Post(
                "alice", "本文です", Instant.parse("2026-05-23T00:00:00Z")));
        postLikeRepository.saveAndFlush(new PostLike(
                post, "abcd1234", Instant.parse("2026-05-23T01:00:00Z")));

        postLikeRepository.saveAndFlush(new PostLike(
                post, "abcd1234", Instant.parse("2026-05-23T02:00:00Z")));

        assertThat(postLikeRepository.countByPostId(post.getId())).isEqualTo(2L);
    }
}
