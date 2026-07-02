package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagRepository tagRepository;

    @Test
    @DisplayName("投稿一覧_51件以上ある場合_新着50件だけを返す")
    void 投稿一覧_51件以上ある場合_新着50件だけを返す() {
        LocalDateTime baseTime = LocalDateTime.parse("2026-05-23T00:00:00");
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < 51; i++) {
            posts.add(new Post("user" + i, "body" + i, baseTime.plusSeconds(i)));
        }
        postRepository.saveAll(posts);

        List<Post> actual = postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();

        assertThat(actual).hasSize(50);
        assertThat(actual).extracting(Post::getBody)
                .containsExactly("body50", "body49", "body48", "body47", "body46",
                        "body45", "body44", "body43", "body42", "body41",
                        "body40", "body39", "body38", "body37", "body36",
                        "body35", "body34", "body33", "body32", "body31",
                        "body30", "body29", "body28", "body27", "body26",
                        "body25", "body24", "body23", "body22", "body21",
                        "body20", "body19", "body18", "body17", "body16",
                        "body15", "body14", "body13", "body12", "body11",
                        "body10", "body9", "body8", "body7", "body6",
                        "body5", "body4", "body3", "body2", "body1");
    }

    @Test
    @DisplayName("投稿検索_本文にキーワードを含む投稿のみ新着順で返す")
    void 投稿検索_本文にキーワードを含む投稿のみ新着順で返す() {
        postRepository.save(new Post("alice", "Java の研修メモです",
                LocalDateTime.parse("2026-05-23T10:00:00")));
        postRepository.save(new Post("bob", "Spring Boot の検索実装です",
                LocalDateTime.parse("2026-05-23T10:01:00")));
        postRepository.save(new Post("carol", "検索とは関係ない投稿です",
                LocalDateTime.parse("2026-05-23T10:02:00")));

        List<Post> actual = postRepository.findByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc("検索");

        assertThat(actual).extracting(Post::getBody)
                .containsExactly("検索とは関係ない投稿です", "Spring Boot の検索実装です");
    }

    @Test
    @DisplayName("投稿一覧_deletedAt設定済みの投稿を返さない")
    void 投稿一覧_deletedAt設定済みの投稿を返さない() {
        Post active = new Post("alice", "表示される投稿です",
                LocalDateTime.parse("2026-05-23T10:00:00"));
        Post deleted = new Post("bob", "削除済み投稿です",
                LocalDateTime.parse("2026-05-23T10:01:00"));
        deleted.markDeleted(LocalDateTime.parse("2026-05-23T10:02:00"));
        postRepository.saveAll(List.of(active, deleted));

        List<Post> actual = postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();

        assertThat(actual).extracting(Post::getBody)
                .containsExactly("表示される投稿です");
    }

    @Test
    @DisplayName("投稿検索_deletedAt設定済みの投稿を返さない")
    void 投稿検索_deletedAt設定済みの投稿を返さない() {
        Post active = new Post("alice", "検索に表示される投稿です",
                LocalDateTime.parse("2026-05-23T10:00:00"));
        Post deleted = new Post("bob", "検索に出したくない投稿です",
                LocalDateTime.parse("2026-05-23T10:01:00"));
        deleted.markDeleted(LocalDateTime.parse("2026-05-23T10:02:00"));
        postRepository.saveAll(List.of(active, deleted));

        List<Post> actual = postRepository.findByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc("検索");

        assertThat(actual).extracting(Post::getBody)
                .containsExactly("検索に表示される投稿です");
    }

    @Test
    @DisplayName("タグ検索_deletedAt設定済みの投稿を返さない")
    void タグ検索_deletedAt設定済みの投稿を返さない() {
        Post active = postRepository.save(new Post("alice", "表示される #Java",
                LocalDateTime.parse("2026-05-23T10:00:00")));
        Post deleted = new Post("bob", "削除済み #Java",
                LocalDateTime.parse("2026-05-23T10:01:00"));
        deleted.markDeleted(LocalDateTime.parse("2026-05-23T10:02:00"));
        Post savedDeleted = postRepository.save(deleted);
        tagRepository.saveAll(List.of(new Tag("Java", active), new Tag("Java", savedDeleted)));

        List<Post> exact = tagRepository.findPostsByNameOrderByCreatedAtDesc("Java");
        List<Post> like = tagRepository.findPostsByNameLikeOrderByCreatedAtDesc("%ava%");

        assertThat(exact).extracting(Post::getBody)
                .containsExactly("表示される #Java");
        assertThat(like).extracting(Post::getBody)
                .containsExactly("表示される #Java");
    }
}
