package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
    }

    @Test
    @DisplayName("投稿一覧_51件以上あるとき_新着50件だけを新着順で返す")
    void 投稿一覧_51件以上あるとき_新着50件だけを新着順で返す() {
        LocalDateTime baseTime = LocalDateTime.parse("2026-05-23T09:00:00");
        List<Post> posts = IntStream.rangeClosed(1, 51)
                .mapToObj(index -> new Post("user" + index, "投稿" + index, baseTime.plusSeconds(index)))
                .toList();
        postRepository.saveAll(posts);

        List<Post> actual = postRepository.findTop50ByOrderByCreatedAtDesc();

        assertThat(actual).hasSize(50);
        assertThat(actual)
                .extracting(Post::getBody)
                .containsExactly(IntStream.iterate(51, index -> index - 1)
                        .limit(50)
                        .mapToObj(index -> "投稿" + index)
                        .toArray(String[]::new));
    }
}
