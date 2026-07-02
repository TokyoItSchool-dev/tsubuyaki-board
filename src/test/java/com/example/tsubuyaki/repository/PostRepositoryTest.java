package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("投稿一覧_51件あるとき_最新50件を新着順で返す")
    void 投稿一覧_51件あるとき_最新50件を新着順で返す() {
        Instant baseTime = Instant.parse("2026-05-23T09:00:00Z");
        List<Post> posts = IntStream.rangeClosed(1, 51)
                .mapToObj(number -> new Post(
                        "user" + number,
                        "post-" + number,
                        baseTime.plusSeconds(number)))
                .toList();
        postRepository.saveAll(posts);

        List<Post> actual = postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();

        assertThat(actual).hasSize(50);
        assertThat(actual.getFirst().getBody()).isEqualTo("post-51");
        assertThat(actual.getLast().getBody()).isEqualTo("post-2");
        assertThat(actual).extracting(Post::getBody).doesNotContain("post-1");
    }

    @Test
    @DisplayName("投稿詳細_投稿が削除されていないとき_投稿を返す")
    void 投稿詳細_投稿が削除されていないとき_投稿を返す() {
        Post post = postRepository.save(new Post(
                "alice",
                "詳細を表示する投稿",
                Instant.parse("2026-05-23T10:00:00Z")));

        Optional<Post> actual = postRepository.findByIdAndDeletedAtIsNull(post.getId());

        assertThat(actual).contains(post);
    }

    @Test
    @DisplayName("投稿検索_本文の一部を指定したとき_LIKEで曖昧検索し新着順で返す")
    void 投稿検索_本文の一部を指定したとき_LIKEで曖昧検索し新着順で返す() {
        postRepository.saveAll(List.of(
                new Post("alice", "明日はリモート勤務です", Instant.parse("2026-05-23T10:00:00Z")),
                new Post("bob", "リモート会議の資料を共有します", Instant.parse("2026-05-23T11:00:00Z")),
                new Post("carol", "出社イベントのお知らせ", Instant.parse("2026-05-23T12:00:00Z"))));

        List<Post> actual = postRepository.findTop50ByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc("リモート");

        assertThat(actual).extracting(Post::getBody)
                .containsExactly("リモート会議の資料を共有します", "明日はリモート勤務です");
    }

    @Test
    @DisplayName("投稿一覧_削除済み投稿があるとき_削除済みを除外して新着順で返す")
    void 投稿一覧_削除済み投稿があるとき_削除済みを除外して新着順で返す() {
        Post deletedPost = new Post("alice", "削除済み投稿", Instant.parse("2026-05-23T12:00:00Z"));
        deletedPost.markDeleted(Instant.parse("2026-05-23T12:30:00Z"));
        postRepository.saveAll(List.of(
                new Post("bob", "表示する新しい投稿", Instant.parse("2026-05-23T11:00:00Z")),
                deletedPost,
                new Post("carol", "表示する古い投稿", Instant.parse("2026-05-23T10:00:00Z"))));

        List<Post> actual = postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();

        assertThat(actual).extracting(Post::getBody)
                .containsExactly("表示する新しい投稿", "表示する古い投稿")
                .doesNotContain("削除済み投稿");
    }

    @Test
    @DisplayName("投稿検索_削除済み投稿が一致するとき_削除済みを除外して返す")
    void 投稿検索_削除済み投稿が一致するとき_削除済みを除外して返す() {
        Post deletedPost = new Post("alice", "リモートの削除済み投稿", Instant.parse("2026-05-23T12:00:00Z"));
        deletedPost.markDeleted(Instant.parse("2026-05-23T12:30:00Z"));
        postRepository.saveAll(List.of(
                deletedPost,
                new Post("bob", "リモート勤務のお知らせ", Instant.parse("2026-05-23T11:00:00Z")),
                new Post("carol", "出社イベントのお知らせ", Instant.parse("2026-05-23T10:00:00Z"))));

        List<Post> actual = postRepository.findTop50ByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc("リモート");

        assertThat(actual).extracting(Post::getBody)
                .containsExactly("リモート勤務のお知らせ")
                .doesNotContain("リモートの削除済み投稿");
    }

    @Test
    @DisplayName("投稿詳細_投稿が削除されているとき_空を返す")
    void 投稿詳細_投稿が削除されているとき_空を返す() {
        Post post = postRepository.save(new Post(
                "alice",
                "削除済み投稿",
                Instant.parse("2026-05-23T10:00:00Z")));
        post.markDeleted(Instant.parse("2026-05-23T11:00:00Z"));
        postRepository.flush();

        Optional<Post> actual = postRepository.findByIdAndDeletedAtIsNull(post.getId());

        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("投稿日時_保存して再取得したとき_UTCのInstantとして復元する")
    void 投稿日時_保存して再取得したとき_UTCのInstantとして復元する() {
        Instant createdAt = Instant.parse("2026-07-01T07:22:13.665Z");
        Instant deletedAt = Instant.parse("2026-07-01T08:22:13.665Z");
        Post post = new Post("alice", "Oracle TIMESTAMP で扱う投稿", createdAt);
        post.markDeleted(deletedAt);
        Post saved = postRepository.saveAndFlush(post);

        entityManager.clear();

        Post actual = postRepository.findById(saved.getId()).orElseThrow();

        assertThat(actual.getCreatedAt()).isEqualTo(createdAt);
        assertThat(actual.getDeletedAt()).isEqualTo(deletedAt);
    }
}
