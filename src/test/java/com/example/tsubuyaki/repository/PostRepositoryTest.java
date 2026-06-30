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
    @DisplayName("投稿一覧_51件以上あるとき_新着50件を新着順で返す")
    void findTop50ByOrderByCreatedAtDesc_over51_returnsLatest50Only() {
        Instant base = Instant.parse("2026-05-23T00:00:00Z");
        for (int i = 1; i <= 51; i++) {
            postRepository.save(new Post("user-" + i, "body-" + i, base.plusSeconds(i)));
        }

        List<Post> posts = postRepository.findTop50ByOrderByCreatedAtDesc();

        assertThat(posts).hasSize(50);
        assertThat(posts.get(0).getAuthor()).isEqualTo("user-51");
        assertThat(posts.get(49).getAuthor()).isEqualTo("user-2");
        assertThat(posts).extracting(Post::getAuthor).doesNotContain("user-1");
    }
}
