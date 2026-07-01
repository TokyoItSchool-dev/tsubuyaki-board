package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
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
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("投稿一覧_51件以上の投稿があるとき_新着50件だけを返す")
    void findLatest_whenMoreThan50Posts_returnsLatest50() {
        Instant baseTime = Instant.parse("2026-05-23T00:00:00Z");
        for (int i = 0; i < 51; i++) {
            postRepository.save(new Post("user" + i, "body" + i, baseTime.plusSeconds(i)));
        }

        List<Post> posts = postRepository.findTop50ByOrderByCreatedAtDesc();

        assertThat(posts).hasSize(50);
        assertThat(posts)
                .extracting(Post::getBody)
                .startsWith("body50", "body49", "body48")
                .doesNotContain("body0");
    }

    @Test
    @DisplayName("投稿検索_キーワードを含む投稿のみ_新着順で最大50件を返す")
    void searchByBodyContaining_whenMatched_returnsLatest50InCreatedAtDesc() {
        Instant baseTime = Instant.parse("2026-05-23T00:00:00Z");
        postRepository.save(new Post("alice", "朝の共有です", baseTime.plusSeconds(1)));
        postRepository.save(new Post("bob", "昼の雑談です", baseTime.plusSeconds(2)));
        for (int i = 0; i < 51; i++) {
            postRepository.save(new Post("user" + i, "検索対象の投稿" + i, baseTime.plusSeconds(10 + i)));
        }

        List<Post> posts = postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("検索対象");

        assertThat(posts).hasSize(50);
        assertThat(posts)
                .extracting(Post::getBody)
                .startsWith("検索対象の投稿50", "検索対象の投稿49", "検索対象の投稿48")
                .doesNotContain("朝の共有です", "昼の雑談です", "検索対象の投稿0");
    }

    @Test
    @DisplayName("投稿検索_マッチする投稿がない場合_空リストを返す")
    void searchByBodyContaining_whenNoMatched_returnsEmptyList() {
        postRepository.save(new Post("alice", "朝の共有です", Instant.parse("2026-05-23T10:00:00Z")));

        List<Post> posts = postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("存在しない");

        assertThat(posts).isEmpty();
    }
}
