package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("投稿一覧_51件以上ある場合_新着50件だけを新着順で返す")
    void 投稿一覧_51件以上ある場合_新着50件だけを新着順で返す() {
        Instant base = Instant.parse("2026-05-23T00:00:00Z");
        IntStream.rangeClosed(0, 50)
                .mapToObj(index -> new Post("user" + index, "body" + index, base.plusSeconds(index)))
                .forEach(postRepository::save);

        var posts = postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();

        assertThat(posts).hasSize(50);
        assertThat(posts.get(0).getAuthor()).isEqualTo("user50");
        assertThat(posts.get(49).getAuthor()).isEqualTo("user1");
        assertThat(posts).extracting(Post::getAuthor).doesNotContain("user0");
    }

    @Test
    @DisplayName("投稿検索_本文にキーワードを含む投稿_新着順で返す")
    void 投稿検索_本文にキーワードを含む投稿_新着順で返す() {
        postRepository.save(new Post("alice", "AI研修のメモ", Instant.parse("2026-05-23T10:00:00Z")));
        postRepository.save(new Post("bob", "雑談です", Instant.parse("2026-05-23T11:00:00Z")));
        postRepository.save(new Post("carol", "今日のAI活用例", Instant.parse("2026-05-23T12:00:00Z")));

        var posts = postRepository.findTop50ByBodyContainingAndDeletedAtIsNullOrderByCreatedAtDesc("AI");

        assertThat(posts).extracting(Post::getAuthor).containsExactly("carol", "alice");
    }

    @Test
    @DisplayName("投稿カラー_保存した投稿_色コードを読み出せる")
    void 投稿カラー_保存した投稿_色コードを読み出せる() {
        Post saved = postRepository.save(new Post(
                "alice",
                "背景色つきの投稿",
                Instant.parse("2026-05-23T10:00:00Z"),
                "FCE7F3"));

        var post = postRepository.findById(saved.getId()).orElseThrow();

        assertThat(post.getColor()).isEqualTo("FCE7F3");
    }

    @Test
    @DisplayName("投稿一覧_削除済み投稿がある場合_未削除投稿だけを返す")
    void 投稿一覧_削除済み投稿がある場合_未削除投稿だけを返す() {
        Post active = postRepository.save(new Post("alice", "表示する投稿", Instant.parse("2026-05-23T10:00:00Z")));
        Post deleted = new Post("bob", "表示しない投稿", Instant.parse("2026-05-23T11:00:00Z"));
        deleted.markDeleted(Instant.parse("2026-05-23T12:00:00Z"));
        postRepository.save(deleted);

        var posts = postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();

        assertThat(posts).extracting(Post::getId).containsExactly(active.getId());
    }

    @Test
    @DisplayName("投稿詳細_削除済み投稿の場合_取得できない")
    void 投稿詳細_削除済み投稿の場合_取得できない() {
        Post deleted = new Post("alice", "削除済み", Instant.parse("2026-05-23T10:00:00Z"));
        deleted.markDeleted(Instant.parse("2026-05-23T11:00:00Z"));
        Post saved = postRepository.save(deleted);

        var actual = postRepository.findByIdAndDeletedAtIsNull(saved.getId());

        assertThat(actual).isEmpty();
    }
}
