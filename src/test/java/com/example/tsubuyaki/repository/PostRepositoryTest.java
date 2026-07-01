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
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("投稿一覧_投稿が51件あるとき_新着順で最大50件を返す")
    void findTop50ByOrderByCreatedAtDesc_投稿が51件あるとき_新着順で最大50件を返す() {
        postRepository.deleteAll();
        Instant base = Instant.parse("2026-05-23T10:00:00Z");
        List<Post> posts = IntStream.rangeClosed(1, 51)
                .mapToObj(index -> new Post(
                        "user" + index,
                        "body" + index,
                        base.plusSeconds(index)))
                .toList();
        postRepository.saveAll(posts);

        List<Post> actual = postRepository.findTop50ByOrderByCreatedAtDesc();

        assertThat(actual).hasSize(50);
        assertThat(actual).extracting(Post::getBody)
                .startsWith("body51", "body50", "body49")
                .doesNotContain("body1");
    }

    @Test
    @DisplayName("キーワード検索_本文に一致する投稿が51件あるとき_新着順で最大50件を返す")
    void findTop50ByBodyContainingIgnoreCaseOrderByCreatedAtDesc_一致する投稿が51件あるとき_新着順で最大50件を返す() {
        postRepository.deleteAll();
        Instant base = Instant.parse("2026-05-23T10:00:00Z");
        List<Post> posts = IntStream.rangeClosed(1, 51)
                .mapToObj(index -> new Post(
                        "user" + index,
                        "keyword body" + index,
                        base.plusSeconds(index)))
                .toList();
        postRepository.saveAll(posts);
        postRepository.save(new Post("other", "no match", base.plusSeconds(100)));

        List<Post> actual = postRepository.findTop50ByBodyContainingIgnoreCaseOrderByCreatedAtDesc("KEYWORD");

        assertThat(actual).hasSize(50);
        assertThat(actual).extracting(Post::getBody)
                .startsWith("keyword body51", "keyword body50", "keyword body49")
                .doesNotContain("keyword body1", "no match");
    }
}
