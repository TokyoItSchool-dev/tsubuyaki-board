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
    @DisplayName("投稿一覧_51件以上の投稿がある場合_DBから新着順で最大50件を取得する")
    void findTop50ByOrderByCreatedAtDesc_overFiftyPosts_returnsLatestFifty() {
        Instant baseTime = Instant.parse("2026-05-23T10:00:00Z");
        IntStream.rangeClosed(1, 51)
                .mapToObj(index -> new Post("user" + index, "body" + index, baseTime.plusSeconds(index)))
                .forEach(postRepository::save);
        postRepository.flush();

        List<Post> posts = postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();

        assertThat(posts).hasSize(50);
        assertThat(posts.get(0).getBody()).isEqualTo("body51");
        assertThat(posts.get(49).getBody()).isEqualTo("body2");
        assertThat(posts).extracting(Post::getBody).doesNotContain("body1");
    }

    @Test
    @DisplayName("投稿削除_削除日時あり_通常一覧から除外されごみ箱に表示される")
    void softDeletedPost_isExcludedFromListAndShownInTrash() {
        Post visiblePost = new Post("alice", "visible", Instant.parse("2026-05-23T10:00:00Z"));
        Post trashedPost = new Post("bob", "trashed", Instant.parse("2026-05-23T10:01:00Z"));
        trashedPost.setDeletedAt(Instant.parse("2026-05-23T11:00:00Z"));
        postRepository.save(visiblePost);
        postRepository.save(trashedPost);
        postRepository.flush();

        assertThat(postRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc(org.springframework.data.domain.Pageable.unpaged())
                .getContent())
                .extracting(Post::getBody)
                .contains("visible")
                .doesNotContain("trashed");
        assertThat(postRepository.findAllByDeletedAtIsNotNullOrderByDeletedAtDesc())
                .extracting(Post::getBody)
                .containsExactly("trashed");
    }
}
