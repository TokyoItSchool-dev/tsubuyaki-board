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
    @DisplayName("投稿一覧_51件以上ある場合_新着50件だけを返す")
    void 投稿一覧_51件以上ある場合_新着50件だけを返す() {
        Instant baseTime = Instant.parse("2026-05-23T00:00:00Z");
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < 51; i++) {
            posts.add(new Post("user" + i, "body" + i, baseTime.plusSeconds(i)));
        }
        postRepository.saveAll(posts);

        List<Post> actual = postRepository.findTop50ByOrderByCreatedAtDesc();

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
}
