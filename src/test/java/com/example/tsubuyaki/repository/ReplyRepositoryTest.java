package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.Reply;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class ReplyRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ReplyRepository replyRepository;

    @Test
    @DisplayName("返信一覧_投稿IDを指定したとき_親子返信を作成日時順で返す")
    void 返信一覧_投稿IDを指定したとき_親子返信を作成日時順で返す() {
        Post post = postRepository.save(new Post(
                "alice",
                "返信対象の投稿",
                Instant.parse("2026-05-23T10:00:00Z")));
        Reply root = replyRepository.save(new Reply(
                post,
                null,
                "bob",
                "親返信",
                Instant.parse("2026-05-23T10:01:00Z")));
        Reply child = replyRepository.save(new Reply(
                post,
                root,
                "carol",
                "子返信",
                Instant.parse("2026-05-23T10:02:00Z")));

        List<Reply> actual = replyRepository.findByPostIdOrderByCreatedAtAscIdAsc(post.getId());

        assertThat(actual).extracting(Reply::getBody).containsExactly("親返信", "子返信");
        assertThat(actual.get(1).getParent()).isEqualTo(root);
        assertThat(actual).contains(child);
    }

    @Test
    @DisplayName("返信取得_投稿IDと返信IDが一致するとき_返信を返す")
    void 返信取得_投稿IDと返信IDが一致するとき_返信を返す() {
        Post post = postRepository.save(new Post(
                "alice",
                "返信対象の投稿",
                Instant.parse("2026-05-23T10:00:00Z")));
        Reply reply = replyRepository.save(new Reply(
                post,
                null,
                "bob",
                "取得する返信",
                Instant.parse("2026-05-23T10:01:00Z")));

        Optional<Reply> actual = replyRepository.findByIdAndPostId(reply.getId(), post.getId());

        assertThat(actual).contains(reply);
    }
}
