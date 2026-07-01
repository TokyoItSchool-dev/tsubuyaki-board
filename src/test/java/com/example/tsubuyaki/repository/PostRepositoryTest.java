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
    @DisplayName("投稿一覧_51件以上あるとき_未削除だけを新着順で最大50件返す")
    void findLatestPosts_51件以上あるとき_未削除だけを新着順で最大50件返す() {
        Instant base = Instant.parse("2026-06-30T00:00:00Z");
        IntStream.rangeClosed(1, 51)
                .mapToObj(index -> new Post(
                        "author" + index,
                        "body" + index,
                        base.plusSeconds(index)))
                .forEach(postRepository::save);
        Post deleted = new Post("deleted", "deleted body", base.plusSeconds(52));
        deleted.markDeleted(base.plusSeconds(53));
        postRepository.save(deleted);

        List<Post> posts = postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();

        assertThat(posts).hasSize(50);
        assertThat(posts.get(0).getBody()).isEqualTo("body51");
        assertThat(posts.get(49).getBody()).isEqualTo("body2");
        assertThat(posts).extracting(Post::getBody).doesNotContain("body1", "deleted body");
    }

    @Test
    @DisplayName("投稿検索_本文にキーワードを含む未削除投稿だけを新着順で返す")
    void searchByBody_本文にキーワードを含む未削除投稿だけを新着順で返す() {
        Instant base = Instant.parse("2026-06-30T00:00:00Z");
        postRepository.save(new Post("alice", "検索対象の共有です", base.plusSeconds(1)));
        postRepository.save(new Post("bob", "検索対象の新しい共有です", base.plusSeconds(2)));
        postRepository.save(new Post("carol", "雑談だけの投稿です", base.plusSeconds(3)));
        Post deleted = new Post("dave", "検索対象だが削除済みです", base.plusSeconds(4));
        deleted.markDeleted(base.plusSeconds(5));
        postRepository.save(deleted);

        List<Post> posts = postRepository.findTop50ByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc("共有");

        assertThat(posts).extracting(Post::getBody)
                .containsExactly("検索対象の新しい共有です", "検索対象の共有です");
    }
}
