package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
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
    @DisplayName("Repository_最新50件取得_新着順で50件だけ返す")
    void Repository_最新50件取得_新着順で50件だけ返す() {
        Instant base = Instant.parse("2026-05-23T10:00:00Z");
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < 51; i++) {
            posts.add(new Post("user" + i, "投稿" + i, base.plusSeconds(i)));
        }
        postRepository.saveAll(posts);

        List<Post> actual = postRepository.findTop50ByOrderByCreatedAtDesc();

        assertThat(actual).hasSize(50);
        assertThat(actual.get(0).getBody()).isEqualTo("投稿50");
        assertThat(actual.get(49).getBody()).isEqualTo("投稿1");
        assertThat(actual)
                .extracting(Post::getCreatedAt)
                .isSortedAccordingTo((left, right) -> right.compareTo(left));
    }
}
