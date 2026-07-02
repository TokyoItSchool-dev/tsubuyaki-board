package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
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

    @Test
    @DisplayName("投稿一覧_51件以上あるとき_新着50件だけを新着順で返す")
    void findTop50ByOrderByCreatedAtDesc_whenMoreThan50Posts_returnsLatest50() {
        LocalDateTime base = LocalDateTime.parse("2026-05-23T00:00:00");
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < 51; i++) {
            posts.add(new Post("user" + i, "body" + i, base.plusSeconds(i)));
        }
        postRepository.saveAll(posts);

        List<Post> actual = postRepository.findTop50ByOrderByCreatedAtDesc();

        assertThat(actual).hasSize(50);
        assertThat(actual).extracting(Post::getBody)
                .startsWith("body50", "body49", "body48")
                .doesNotContain("body0");
    }

    @Test
    @DisplayName("投稿検索_本文にキーワードを含むとき_本文に一致する投稿だけを新着順で返す")
    void findTop50ByBodyContainingOrderByCreatedAtDesc_whenBodyContainsKeyword_returnsMatchedPosts() {
        LocalDateTime base = LocalDateTime.parse("2026-05-23T00:00:00");
        Post oldMatched = new Post("alice", "朝会の共有事項", base.plusHours(1));
        Post notMatched = new Post("bob", "ランチの相談", base.plusHours(2));
        Post authorOnlyMatched = new Post("朝会担当", "別件の連絡", base.plusHours(3));
        Post newMatched = new Post("carol", "明日の朝会メモ", base.plusHours(4));
        postRepository.saveAll(List.of(oldMatched, notMatched, authorOnlyMatched, newMatched));

        List<Post> actual = postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("朝会");

        assertThat(actual).extracting(Post::getBody)
                .containsExactly("明日の朝会メモ", "朝会の共有事項");
    }

    @Test
    @DisplayName("投稿検索_51件以上一致するとき_新着50件だけを返す")
    void findTop50ByBodyContainingOrderByCreatedAtDesc_whenMoreThan50PostsMatch_returnsLatest50() {
        LocalDateTime base = LocalDateTime.parse("2026-05-23T00:00:00");
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < 51; i++) {
            posts.add(new Post("user" + i, "検索対象 body" + i, base.plusSeconds(i)));
        }
        postRepository.saveAll(posts);

        List<Post> actual = postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("検索対象");

        assertThat(actual).hasSize(50);
        assertThat(actual).extracting(Post::getBody)
                .startsWith("検索対象 body50", "検索対象 body49", "検索対象 body48")
                .doesNotContain("検索対象 body0");
    }
}
