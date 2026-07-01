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
    @DisplayName("いいね数_countByPostId_指定投稿のいいね数だけ返す")
    void countByPostId_returnsLikesForSpecifiedPostOnly() {
        Post target = postRepository.save(new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        Post other = postRepository.save(new Post("bob", "hi", Instant.parse("2026-05-23T10:01:00Z")));
        postLikeRepository.save(new PostLike(target, "aaaaaaaa", Instant.parse("2026-05-23T10:02:00Z")));
        postLikeRepository.save(new PostLike(target, "bbbbbbbb", Instant.parse("2026-05-23T10:03:00Z")));
        postLikeRepository.save(new PostLike(other, "cccccccc", Instant.parse("2026-05-23T10:04:00Z")));

        long actual = postLikeRepository.countByPostId(target.getId());

        assertThat(actual).isEqualTo(2);
    }

    @Test
    @DisplayName("いいね検索_findByPostIdAndClientHash_同じ投稿とclientHashのいいねを返す")
    void findByPostIdAndClientHash_returnsMatchingLike() {
        Post post = postRepository.save(new Post("alice", "hello", Instant.parse("2026-05-23T10:00:00Z")));
        PostLike like = postLikeRepository.save(
                new PostLike(post, "aaaaaaaa", Instant.parse("2026-05-23T10:02:00Z")));

        var actual = postLikeRepository.findByPostIdAndClientHash(post.getId(), "aaaaaaaa");

        assertThat(actual).containsSame(like);
    }
}
