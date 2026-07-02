package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostReply;
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
class PostReplyRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostReplyRepository postReplyRepository;

    @Test
    @DisplayName("返信一覧_投稿IDで投稿日時の昇順に取得できる")
    void findByPostIdOrderByCreatedAtAsc_returnsRepliesOldestFirst() {
        Post post = postRepository.save(new Post("alice", "投稿本文", Instant.parse("2026-07-02T10:00:00Z")));
        Post otherPost = postRepository.save(new Post("bob", "別投稿", Instant.parse("2026-07-02T10:01:00Z")));
        PostReply oldReply = postReplyRepository.save(new PostReply(
                post,
                "yamada",
                "ありがとうございます！",
                Instant.parse("2026-07-02T10:02:00Z"),
                "blue"
        ));
        PostReply newReply = postReplyRepository.save(new PostReply(
                post,
                "sato",
                "参考になりました！",
                Instant.parse("2026-07-02T10:03:00Z"),
                "green"
        ));
        postReplyRepository.save(new PostReply(
                otherPost,
                "other",
                "別投稿への返信",
                Instant.parse("2026-07-02T10:04:00Z"),
                "red"
        ));

        List<PostReply> replies = postReplyRepository.findByPostIdOrderByCreatedAtAsc(post.getId());

        assertThat(replies).containsExactly(oldReply, newReply);
    }

    @Test
    @DisplayName("返信数_投稿IDで現在登録されている返信数を取得できる")
    void countByPostId_returnsReplyCount() {
        Post post = postRepository.save(new Post("alice", "投稿本文", Instant.parse("2026-07-02T10:00:00Z")));
        postReplyRepository.save(new PostReply(post, "yamada", "返信1", Instant.parse("2026-07-02T10:01:00Z"), "gray"));
        postReplyRepository.save(new PostReply(post, "sato", "返信2", Instant.parse("2026-07-02T10:02:00Z"), "gray"));

        assertThat(postReplyRepository.countByPostId(post.getId())).isEqualTo(2L);
    }
}
