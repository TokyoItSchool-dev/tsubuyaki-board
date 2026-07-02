package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostComment;
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
class PostCommentRepositoryTest {

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("コメント一覧_投稿idに対応するコメントを新しい順で返す")
    void コメント一覧_投稿idに対応するコメントを新しい順で返す() {
        Instant baseTime = Instant.parse("2026-05-23T00:00:00Z");
        Post post = postRepository.save(new Post("alice", "投稿本文", baseTime));
        Post otherPost = postRepository.save(new Post("bob", "別投稿本文", baseTime));
        postCommentRepository.save(new PostComment(post.getId(), "alice", "古いコメント", "red", baseTime.plusSeconds(1)));
        postCommentRepository.save(new PostComment(post.getId(), "bob", "新しいコメント", "green", baseTime.plusSeconds(2)));
        postCommentRepository.save(new PostComment(otherPost.getId(), "carol", "別投稿のコメント", "purple",
                baseTime.plusSeconds(3)));

        List<PostComment> comments = postCommentRepository.findByPostIdOrderByCreatedAtDesc(post.getId());

        assertThat(comments).extracting(PostComment::getBody)
                .containsExactly("新しいコメント", "古いコメント");
    }

    @Test
    @DisplayName("コメント件数_投稿idに対応するコメント件数を返す")
    void コメント件数_投稿idに対応するコメント件数を返す() {
        Instant baseTime = Instant.parse("2026-05-23T00:00:00Z");
        Post post = postRepository.save(new Post("alice", "投稿本文", baseTime));
        Post otherPost = postRepository.save(new Post("bob", "別投稿本文", baseTime));
        postCommentRepository.save(new PostComment(post.getId(), "alice", "1件目", "red", baseTime.plusSeconds(1)));
        postCommentRepository.save(new PostComment(post.getId(), "bob", "2件目", "green", baseTime.plusSeconds(2)));
        postCommentRepository.save(new PostComment(otherPost.getId(), "carol", "別投稿", "purple",
                baseTime.plusSeconds(3)));

        long count = postCommentRepository.countByPostId(post.getId());

        assertThat(count).isEqualTo(2L);
    }
}
