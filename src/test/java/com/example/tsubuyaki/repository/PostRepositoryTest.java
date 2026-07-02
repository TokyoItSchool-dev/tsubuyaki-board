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
    @DisplayName("投稿一覧_51件あるとき_新着順で最大50件を返す")
    void 投稿一覧_51件あるとき_新着順で最大50件を返す() {
        LocalDateTime base = LocalDateTime.of(2026, 6, 26, 9, 0);
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < 51; i++) {
            posts.add(new Post("user" + i, "title" + i, "body" + i, base.plusSeconds(i)));
        }
        postRepository.saveAll(posts);
        postRepository.flush();

        List<Post> actual = postRepository.findTop50ByOrderByCreatedAtDesc();

        assertThat(actual).hasSize(50);
        assertThat(actual).extracting(Post::getBody)
                .startsWith("body50", "body49", "body48")
                .doesNotContain("body0");
    }

    @Test
    @DisplayName("投稿検索_本文に検索ワードを含む投稿_新着順で最大50件を返す")
    void 投稿検索_本文に検索ワードを含む投稿_新着順で最大50件を返す() {
        postRepository.save(new Post("suzuki", "古い一致", "AI研修のメモ", LocalDateTime.of(2026, 7, 2, 9, 0)));
        postRepository.save(new Post("tanaka", "不一致", "ランチのメモ", LocalDateTime.of(2026, 7, 2, 10, 0)));
        postRepository.save(new Post("sato", "新しい一致", "明日のAI研修", LocalDateTime.of(2026, 7, 2, 11, 0)));
        postRepository.flush();

        List<Post> actual = postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("AI研修");

        assertThat(actual).extracting(Post::getTitle)
                .containsExactly("新しい一致", "古い一致");
    }
}
