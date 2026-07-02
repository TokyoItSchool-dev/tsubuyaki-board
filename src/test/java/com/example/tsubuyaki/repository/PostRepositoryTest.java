package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.Tag;
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

    @Autowired
    private TagRepository tagRepository;

    @Test
    @DisplayName("Repository_投稿一覧_最新50件を新着順で返す")
    void 投稿一覧_投稿が55件あるとき_最新50件を新着順で返す() {
        Instant base = Instant.parse("2026-06-26T09:00:00Z");
        List<Post> posts = IntStream.rangeClosed(1, 55)
                .mapToObj(number -> new Post("user" + number, "body" + number, base.plusSeconds(number)))
                .toList();
        postRepository.saveAll(posts);

        List<Post> latestPosts = postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();

        List<String> expectedBodies = IntStream.iterate(55, number -> number - 1)
                .limit(50)
                .mapToObj(number -> "body" + number)
                .toList();
        assertThat(latestPosts)
                .hasSize(50)
                .extracting(Post::getBody)
                .containsExactlyElementsOf(expectedBodies);
    }

    @Test
    @DisplayName("Repository_投稿登録_投稿を保存できる")
    void 投稿登録_投稿を保存できる() {
        Post post = new Post("alice", "M3 の投稿", "green", Instant.parse("2026-06-26T10:00:00Z"));

        Post savedPost = postRepository.saveAndFlush(post);

        assertThat(savedPost.getId()).isNotNull();
        assertThat(postRepository.findById(savedPost.getId()))
                .get()
                .extracting(Post::getAuthor, Post::getBody, Post::getAvatarColor, Post::getCreatedAt)
                .containsExactly("alice", "M3 の投稿", "green", Instant.parse("2026-06-26T10:00:00Z"));
    }

    @Test
    @DisplayName("Repository_投稿詳細_id指定で投稿を取得できる")
    void 投稿詳細_id指定で投稿を取得できる() {
        Post savedPost = postRepository.saveAndFlush(
                new Post("alice", "M4 の詳細投稿", Instant.parse("2026-06-26T11:00:00Z")));

        assertThat(postRepository.findByIdAndDeletedAtIsNull(savedPost.getId()))
                .get()
                .extracting(Post::getAuthor, Post::getBody, Post::getCreatedAt)
                .containsExactly("alice", "M4 の詳細投稿", Instant.parse("2026-06-26T11:00:00Z"));
    }

    @Test
    @DisplayName("Repository_投稿詳細_削除済み投稿を取得しない")
    void 投稿詳細_削除済み投稿のid指定_空を返す() {
        Post deletedPost = new Post("alice", "削除済み詳細投稿", Instant.parse("2026-06-26T11:00:00Z"));
        deletedPost.delete(Instant.parse("2026-06-26T12:00:00Z"));
        Post savedPost = postRepository.saveAndFlush(deletedPost);

        assertThat(postRepository.findByIdAndDeletedAtIsNull(savedPost.getId())).isEmpty();
    }

    @Test
    @DisplayName("Repository_キーワード検索_body部分一致を新着順で返す")
    void キーワード検索_body部分一致を新着順で返す() {
        postRepository.save(new Post("alice", "朝会で検索機能を相談", Instant.parse("2026-06-26T09:00:00Z")));
        postRepository.save(new Post("bob", "ランチの話題", Instant.parse("2026-06-26T10:00:00Z")));
        postRepository.save(new Post("carol", "検索フォームを実装", Instant.parse("2026-06-26T11:00:00Z")));
        postRepository.flush();

        List<Post> posts = postRepository.findTop50ByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc("検索");

        assertThat(posts)
                .extracting(Post::getBody)
                .containsExactly("検索フォームを実装", "朝会で検索機能を相談");
    }

    @Test
    @DisplayName("Repository_タグ別一覧_タグ名に関連する投稿を新着順で返す")
    void タグ別一覧_タグ名に関連する投稿を新着順で返す() {
        Tag javaTag = tagRepository.save(new Tag("java"));
        Tag springTag = tagRepository.save(new Tag("spring"));
        Post oldPost = new Post("alice", "#java の古い投稿", Instant.parse("2026-06-26T09:00:00Z"));
        oldPost.addTag(javaTag);
        Post otherPost = new Post("bob", "#spring の投稿", Instant.parse("2026-06-26T10:00:00Z"));
        otherPost.addTag(springTag);
        Post newPost = new Post("carol", "#java の新しい投稿", Instant.parse("2026-06-26T11:00:00Z"));
        newPost.addTag(javaTag);
        postRepository.saveAll(List.of(oldPost, otherPost, newPost));
        postRepository.flush();

        List<Post> posts = postRepository.findTop50DistinctByDeletedAtIsNullAndTagsNameOrderByCreatedAtDesc("java");

        assertThat(posts)
                .extracting(Post::getBody)
                .containsExactly("#java の新しい投稿", "#java の古い投稿");
    }

    @Test
    @DisplayName("Repository_投稿一覧_削除済み投稿を除外する")
    void 投稿一覧_削除済み投稿があるとき_未削除投稿だけを返す() {
        Post activePost = new Post("alice", "表示される投稿", Instant.parse("2026-06-26T10:00:00Z"));
        Post deletedPost = new Post("bob", "削除済み投稿", Instant.parse("2026-06-26T11:00:00Z"));
        deletedPost.delete(Instant.parse("2026-06-26T12:00:00Z"));
        postRepository.saveAll(List.of(activePost, deletedPost));
        postRepository.flush();

        List<Post> posts = postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();

        assertThat(posts)
                .extracting(Post::getBody)
                .containsExactly("表示される投稿");
    }

    @Test
    @DisplayName("Repository_キーワード検索_削除済み投稿を除外する")
    void キーワード検索_削除済み投稿が一致するとき_未削除投稿だけを返す() {
        Post activePost = new Post("alice", "検索できる投稿", Instant.parse("2026-06-26T10:00:00Z"));
        Post deletedPost = new Post("bob", "検索できない削除済み投稿", Instant.parse("2026-06-26T11:00:00Z"));
        deletedPost.delete(Instant.parse("2026-06-26T12:00:00Z"));
        postRepository.saveAll(List.of(activePost, deletedPost));
        postRepository.flush();

        List<Post> posts = postRepository.findTop50ByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc("検索");

        assertThat(posts)
                .extracting(Post::getBody)
                .containsExactly("検索できる投稿");
    }

    @Test
    @DisplayName("Repository_タグ別一覧_削除済み投稿を除外する")
    void タグ別一覧_削除済み投稿が一致するとき_未削除投稿だけを返す() {
        Tag javaTag = tagRepository.save(new Tag("java"));
        Post activePost = new Post("alice", "#java 表示される投稿", Instant.parse("2026-06-26T10:00:00Z"));
        activePost.addTag(javaTag);
        Post deletedPost = new Post("bob", "#java 削除済み投稿", Instant.parse("2026-06-26T11:00:00Z"));
        deletedPost.addTag(javaTag);
        deletedPost.delete(Instant.parse("2026-06-26T12:00:00Z"));
        postRepository.saveAll(List.of(activePost, deletedPost));
        postRepository.flush();

        List<Post> posts = postRepository.findTop50DistinctByDeletedAtIsNullAndTagsNameOrderByCreatedAtDesc("java");

        assertThat(posts)
                .extracting(Post::getBody)
                .containsExactly("#java 表示される投稿");
    }
}
