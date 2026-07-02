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
    @DisplayName("投稿一覧_51件以上あるとき_新着50件だけを返す")
    void findTop50ByOrderByCreatedAtDesc_whenMoreThan50Posts_returnsLatest50() {
        Instant baseTime = Instant.parse("2026-05-23T10:00:00Z");
        List<Post> posts = new ArrayList<>();
        for (int index = 1; index <= 51; index++) {
            posts.add(new Post((long) index, "user" + index, "body" + index, baseTime.plusSeconds(index)));
        }
        postRepository.saveAll(posts);

        List<Post> latestPosts = postRepository.findTop50ByOrderByCreatedAtDesc();

        assertThat(latestPosts).hasSize(50);
        assertThat(latestPosts.get(0).getAuthor()).isEqualTo("user51");
        assertThat(latestPosts.get(49).getAuthor()).isEqualTo("user2");
        assertThat(latestPosts).extracting(Post::getAuthor).doesNotContain("user1");
    }

    @Test
    @DisplayName("投稿検索_本文部分一致_一致する投稿だけを新着順で返す")
    void findTop50ByBodyContainingOrderByCreatedAtDesc_whenBodyMatches_returnsMatchesInCreatedAtDesc() {
        Instant baseTime = Instant.parse("2026-05-23T10:00:00Z");
        postRepository.saveAll(List.of(
                new Post(1L, "alice", "研修でSpringを学ぶ", baseTime.plusSeconds(1)),
                new Post(2L, "bob", "雑談のみ", baseTime.plusSeconds(3)),
                new Post(3L, "carol", "Spring Boot実装", baseTime.plusSeconds(2))));

        List<Post> actual = postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("Spring");

        assertThat(actual).extracting(Post::getAuthor).containsExactly("carol", "alice");
    }

    @Test
    @DisplayName("投稿検索_検索条件にLIKE特殊文字が含まれるとき_文字そのものとして検索する")
    void findTop50ByBodyContainingOrderByCreatedAtDesc_whenQueryContainsLikeWildcards_treatsThemAsLiterals() {
        Instant baseTime = Instant.parse("2026-05-23T10:00:00Z");
        postRepository.saveAll(List.of(
                new Post(1L, "percent", "達成率は100%です", baseTime.plusSeconds(1)),
                new Post(2L, "plain", "達成率は100点です", baseTime.plusSeconds(2)),
                new Post(3L, "underscore", "コードA_B", baseTime.plusSeconds(3)),
                new Post(4L, "backslash", "パスC:\\work", baseTime.plusSeconds(4))));

        List<Post> percentMatches = postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("100%");
        List<Post> underscoreMatches = postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("A_B");
        List<Post> backslashMatches = postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("C:\\");

        assertThat(percentMatches).extracting(Post::getAuthor).containsExactly("percent");
        assertThat(underscoreMatches).extracting(Post::getAuthor).containsExactly("underscore");
        assertThat(backslashMatches).extracting(Post::getAuthor).containsExactly("backslash");
    }

    @Test
    @DisplayName("投稿検索_51件以上一致したとき_新着50件だけを返す")
    void findTop50ByBodyContainingOrderByCreatedAtDesc_whenMoreThan50Matches_returnsLatest50() {
        Instant baseTime = Instant.parse("2026-05-23T10:00:00Z");
        List<Post> posts = new ArrayList<>();
        for (int index = 1; index <= 51; index++) {
            posts.add(new Post((long) index, "user" + index, "検索対象 body" + index, baseTime.plusSeconds(index)));
        }
        posts.add(new Post(100L, "other", "対象外", baseTime.plusSeconds(100)));
        postRepository.saveAll(posts);

        List<Post> actual = postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("検索対象");

        assertThat(actual).hasSize(50);
        assertThat(actual.get(0).getAuthor()).isEqualTo("user51");
        assertThat(actual.get(49).getAuthor()).isEqualTo("user2");
        assertThat(actual).extracting(Post::getAuthor).doesNotContain("user1", "other");
    }

    @Test
    @DisplayName("投稿登録_最大長の投稿者本文投稿日を保存したとき_IDと各項目をDBに登録できる")
    void save_whenAuthorAndBodyAreMaxLength_registersIdAuthorBodyAndCreatedAt() {
        String author = "a".repeat(30);
        String body = "b".repeat(280);
        Instant createdAt = Instant.parse("2026-05-23T10:00:00Z");
        postRepository.saveAndFlush(new Post(10L, "existing", "body", createdAt.minusSeconds(1)));

        Post saved = postRepository.saveAndFlush(new Post(11L, author, body, createdAt));

        assertThat(saved.getId()).isEqualTo(11L);
        assertThat(postRepository.findById(saved.getId()))
                .get()
                .satisfies(post -> {
                    assertThat(post.getId()).isEqualTo(11L);
                    assertThat(post.getAuthor()).isEqualTo(author);
                    assertThat(post.getBody()).isEqualTo(body);
                    assertThat(post.getCreatedAt()).isEqualTo(createdAt);
                });
    }

    @Test
    @DisplayName("投稿登録_既存投稿があるとき_最大IDを返す")
    void findMaxId_whenPostsExist_returnsMaxId() {
        Instant createdAt = Instant.parse("2026-05-23T10:00:00Z");
        postRepository.saveAndFlush(new Post(10L, "alice", "hello", createdAt));
        postRepository.saveAndFlush(new Post(15L, "bob", "hi", createdAt.plusSeconds(1)));

        Long maxId = postRepository.findMaxId();

        assertThat(maxId).isEqualTo(15L);
    }

    @Test
    @DisplayName("投稿登録_既存投稿がないとき_最大IDは0を返す")
    void findMaxId_whenNoPosts_returnsZero() {
        Long maxId = postRepository.findMaxId();

        assertThat(maxId).isZero();
    }
}
