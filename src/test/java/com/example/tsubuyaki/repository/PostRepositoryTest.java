package com.example.tsubuyaki.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("保存_save_投稿を保存しIDを採番する")
    void 保存_save_投稿を保存しIDを採番する() {
        PostEntity post = new PostEntity("alice", "RED", "hello", Instant.parse("2026-06-26T09:00:00Z"));

        PostEntity saved = postRepository.saveAndFlush(post);
        entityManager.clear();

        assertThat(saved.getId()).isNotNull();
        assertThat(postRepository.findById(saved.getId()))
                .get()
                .satisfies(found -> {
                    assertThat(found.getAuthor()).isEqualTo("alice");
                    assertThat(found.getAvatarColor()).isEqualTo("RED");
                    assertThat(found.getBody()).isEqualTo("hello");
                    assertThat(found.getCreatedAt()).isEqualTo(Instant.parse("2026-06-26T09:00:00Z"));
                });
    }

    @Test
    @DisplayName("取得_findById_存在するIDはOptionalに投稿を入れて返す")
    void 取得_findById_存在するIDはOptionalに投稿を入れて返す() {
        PostEntity saved = persistPost("alice", "find me", Instant.parse("2026-06-26T09:00:00Z"));

        Optional<PostEntity> actual = postRepository.findById(saved.getId());

        assertThat(actual)
                .get()
                .satisfies(post -> {
                    assertThat(post.getId()).isEqualTo(saved.getId());
                    assertThat(post.getAuthor()).isEqualTo("alice");
                    assertThat(post.getBody()).isEqualTo("find me");
                });
    }

    @Test
    @DisplayName("取得_findById_存在しないIDはOptional_emptyを返す")
    void 取得_findById_存在しないIDはOptional_emptyを返す() {
        Optional<PostEntity> actual = postRepository.findById(999_999L);

        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("一覧_findAll_保存済み投稿を全件取得できる")
    void 一覧_findAll_保存済み投稿を全件取得できる() {
        persistPost("alice", "first", Instant.parse("2026-06-26T09:00:00Z"));
        persistPost("bob", "second", Instant.parse("2026-06-26T10:00:00Z"));

        List<PostEntity> posts = postRepository.findAll();

        assertThat(posts)
                .extracting(PostEntity::getAuthor)
                .containsExactlyInAnyOrder("alice", "bob");
    }

    @Test
    @DisplayName("投稿一覧_異なる投稿日時であるとき_createdAt降順で返す")
    void 投稿一覧_異なる投稿日時であるとき_createdAt降順で返す() {
        persistPost("old", "body", Instant.parse("2026-06-26T09:00:00Z"));
        persistPost("new", "body", Instant.parse("2026-06-26T11:00:00Z"));
        persistPost("middle", "body", Instant.parse("2026-06-26T10:00:00Z"));

        List<PostEntity> posts = postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDescIdDesc();

        assertThat(posts)
                .extracting(PostEntity::getAuthor)
                .containsExactly("new", "middle", "old");
    }

    @Test
    @DisplayName("投稿一覧_論理削除済み投稿_取得されない")
    void 投稿一覧_論理削除済み投稿_取得されない() {
        persistPost("active", "visible", Instant.parse("2026-06-26T09:00:00Z"));
        PostEntity deleted = persistPost("deleted", "hidden", Instant.parse("2026-06-26T10:00:00Z"));
        deleted.markDeleted(Instant.parse("2026-06-26T11:00:00Z"));
        entityManager.merge(deleted);
        flushAndClear();

        List<PostEntity> posts = postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDescIdDesc();

        assertThat(posts)
                .extracting(PostEntity::getAuthor)
                .containsExactly("active");
    }

    @Test
    @DisplayName("投稿一覧_51件以上同時刻であるとき_id降順で50件だけを返す")
    void 投稿一覧_51件以上同時刻であるとき_id降順で50件だけを返す() {
        Instant base = Instant.parse("2026-06-26T09:00:00Z");
        for (int i = 1; i <= 51; i++) {
            entityManager.persist(new PostEntity("user" + i, "BLUE", "body" + i, base));
        }
        flushAndClear();

        List<PostEntity> posts = postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDescIdDesc();

        assertThat(posts).hasSize(50);
        assertThat(posts).extracting(PostEntity::getAuthor)
                .startsWith("user51", "user50", "user49")
                .doesNotContain("user1");
    }

    @Test
    @DisplayName("投稿検索_本文にキーワードを含む投稿だけをcreatedAt降順で返す")
    void 投稿検索_本文にキーワードを含む投稿だけをcreatedAt降順で返す() {
        persistPost("old", "Spring のメモ", Instant.parse("2026-06-26T09:00:00Z"));
        persistPost("new", "Spring Boot の共有", Instant.parse("2026-06-26T11:00:00Z"));
        persistPost("other", "Java の共有", Instant.parse("2026-06-26T10:00:00Z"));

        List<PostEntity> posts = postRepository.findTop50ByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDescIdDesc("Spring");

        assertThat(posts)
                .extracting(PostEntity::getAuthor)
                .containsExactly("new", "old");
    }

    @Test
    @DisplayName("投稿検索_該当なし_空リストを返す")
    void 投稿検索_該当なし_空リストを返す() {
        persistPost("alice", "Java の共有", Instant.parse("2026-06-26T09:00:00Z"));

        List<PostEntity> posts = postRepository.findTop50ByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDescIdDesc("NoHit");

        assertThat(posts).isEmpty();
    }

    @Test
    @DisplayName("投稿詳細_ID検索_論理削除済み投稿はOptional_emptyを返す")
    void 投稿詳細_ID検索_論理削除済み投稿はOptional_emptyを返す() {
        PostEntity deleted = persistPost("alice", "hidden", Instant.parse("2026-06-26T09:00:00Z"));
        deleted.markDeleted(Instant.parse("2026-06-26T10:00:00Z"));
        entityManager.merge(deleted);
        flushAndClear();

        Optional<PostEntity> actual = postRepository.findByIdAndDeletedAtIsNull(deleted.getId());

        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("タグ別一覧_指定タグに関連する投稿だけをcreatedAt降順で返す")
    void タグ別一覧_指定タグに関連する投稿だけをcreatedAt降順で返す() {
        TagEntity java = entityManager.persistAndFlush(new TagEntity("Java"));
        TagEntity spring = entityManager.persistAndFlush(new TagEntity("Spring"));
        entityManager.persist(new PostEntity(
                null,
                "old",
                "BLUE",
                "Java の共有 #Java",
                Instant.parse("2026-06-26T09:00:00Z"),
                List.of(java)));
        entityManager.persist(new PostEntity(
                null,
                "new",
                "GREEN",
                "Spring と Java の共有 #Spring #Java",
                Instant.parse("2026-06-26T11:00:00Z"),
                List.of(java, spring)));
        entityManager.persist(new PostEntity(
                null,
                "other",
                "ORANGE",
                "Spring の共有 #Spring",
                Instant.parse("2026-06-26T10:00:00Z"),
                List.of(spring)));
        flushAndClear();

        List<PostEntity> posts = postRepository.findTop50ByDeletedAtIsNullAndTagsNameOrderByCreatedAtDescIdDesc("Java");

        assertThat(posts)
                .extracting(PostEntity::getAuthor)
                .containsExactly("new", "old");
        assertThat(posts.get(0).getTags())
                .extracting(TagEntity::getName)
                .containsExactly("Java", "Spring");
    }

    @Test
    @DisplayName("更新_save_同じIDの投稿を保存すると内容を更新できる")
    void 更新_save_同じIDの投稿を保存すると内容を更新できる() {
        PostEntity saved = persistPost("alice", "before", Instant.parse("2026-06-26T09:00:00Z"));
        PostEntity updated = new PostEntity(
                saved.getId(),
                "alice",
                "GREEN",
                "after",
                Instant.parse("2026-06-26T10:00:00Z"));

        postRepository.saveAndFlush(updated);
        entityManager.clear();

        assertThat(postRepository.findById(saved.getId()))
                .get()
                .satisfies(post -> {
                    assertThat(post.getBody()).isEqualTo("after");
                    assertThat(post.getAvatarColor()).isEqualTo("GREEN");
                    assertThat(post.getCreatedAt()).isEqualTo(Instant.parse("2026-06-26T10:00:00Z"));
                });
    }

    @Test
    @DisplayName("削除_delete_指定した投稿を削除できる")
    void 削除_delete_指定した投稿を削除できる() {
        PostEntity saved = persistPost("alice", "delete me", Instant.parse("2026-06-26T09:00:00Z"));

        postRepository.delete(saved);
        postRepository.flush();
        entityManager.clear();

        assertThat(postRepository.findById(saved.getId())).isEmpty();
    }

    private PostEntity persistPost(String author, String body, Instant createdAt) {
        PostEntity saved = entityManager.persistAndFlush(new PostEntity(author, "BLUE", body, createdAt));
        entityManager.clear();
        return saved;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
