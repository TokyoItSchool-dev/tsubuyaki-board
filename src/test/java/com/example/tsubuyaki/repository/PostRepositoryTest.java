package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostTag;
import com.example.tsubuyaki.domain.Tag;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.ArrayList;
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
    private TagRepository tagRepository;

    @Autowired
    private PostTagRepository postTagRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("投稿一覧_51件以上の投稿がある場合_新着50件だけを返す")
    void findTop50ByOrderByCreatedAtDesc_moreThan51_returnsLatest50() {
        Instant base = Instant.parse("2026-05-23T00:00:00Z");
        List<Post> posts = new ArrayList<>();
        for (int i = 1; i <= 51; i++) {
            posts.add(new Post("user" + i, "body" + i, base.plusSeconds(i)));
        }
        postRepository.saveAll(posts);

        List<Post> latestPosts = postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();

        assertThat(latestPosts).hasSize(50);
        assertThat(latestPosts.get(0).getBody()).isEqualTo("body51");
        assertThat(latestPosts.get(49).getBody()).isEqualTo("body2");
        assertThat(latestPosts).extracting(Post::getBody).doesNotContain("body1");
    }

    @Test
    @DisplayName("投稿一覧_ID取得_新着50件のidだけを返す")
    void findLatestIds_moreThan51_returnsLatest50Ids() {
        Instant base = Instant.parse("2026-05-23T00:00:00Z");
        List<Post> posts = new ArrayList<>();
        for (int i = 1; i <= 51; i++) {
            posts.add(new Post("user" + i, "body" + i, base.plusSeconds(i)));
        }
        postRepository.saveAll(posts);
        flushAndClear();

        List<Long> ids = postRepository.findLatestIds(PageRequest.of(0, 50));
        List<Post> latestPosts = postRepository.findAllById(ids);

        assertThat(ids).hasSize(50);
        assertThat(latestPosts).extracting(Post::getBody).doesNotContain("body1");
    }

    @Test
    @DisplayName("投稿検索_本文にキーワードを含む投稿だけを新着順で返す")
    void findByBodyContainingOrderByCreatedAtDesc_returnsMatchingPosts() {
        postRepository.saveAll(List.of(
                new Post("alice", "abcを含む古い投稿", Instant.parse("2026-05-23T01:00:00Z")),
                new Post("bob", "含まない投稿", Instant.parse("2026-05-23T02:00:00Z")),
                new Post("carol", "新しいabc投稿", Instant.parse("2026-05-23T03:00:00Z"))
        ));

        List<Post> posts = postRepository.findByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc("abc");

        assertThat(posts)
                .extracting(Post::getBody)
                .containsExactly("新しいabc投稿", "abcを含む古い投稿");
    }

    @Test
    @DisplayName("投稿編集_保存済み投稿を更新すると変更後の値で取得できる")
    void save_whenExistingPostUpdated_persistsUpdatedValues() {
        Post post = postRepository.save(new Post(
                "alice", "更新前本文です", "blue", Instant.parse("2026-05-23T00:00:00Z")));

        post.update("bob", "更新後本文です", "green");
        postRepository.saveAndFlush(post);

        assertThat(postRepository.findByIdAndDeletedAtIsNull(post.getId()))
                .get()
                .satisfies(updated -> {
                    assertThat(updated.getAuthor()).isEqualTo("bob");
                    assertThat(updated.getBody()).isEqualTo("更新後本文です");
                    assertThat(updated.getAvatarColor()).isEqualTo("green");
                    assertThat(updated.getCreatedAt()).isEqualTo(Instant.parse("2026-05-23T00:00:00Z"));
                    assertThat(updated.getUpdatedAt()).isNotNull();
                });
    }

    @Test
    @DisplayName("投稿一覧_削除済み投稿_新着一覧に表示しない")
    void findTop50ByDeletedAtIsNullOrderByCreatedAtDesc_excludesDeletedPosts() {
        Post activePost = new Post("alice", "表示する投稿", Instant.parse("2026-05-23T01:00:00Z"));
        Post deletedPost = new Post("bob", "表示しない投稿", Instant.parse("2026-05-23T02:00:00Z"));
        deletedPost.delete(Instant.parse("2026-05-24T00:00:00Z"));
        postRepository.saveAll(List.of(activePost, deletedPost));

        List<Post> posts = postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();

        assertThat(posts)
                .extracting(Post::getBody)
                .containsExactly("表示する投稿");
    }

    @Test
    @DisplayName("投稿検索_削除済み投稿_検索結果に表示しない")
    void findByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc_excludesDeletedPosts() {
        Post activePost = new Post("alice", "abc 表示する投稿", Instant.parse("2026-05-23T01:00:00Z"));
        Post deletedPost = new Post("bob", "abc 表示しない投稿", Instant.parse("2026-05-23T02:00:00Z"));
        deletedPost.delete(Instant.parse("2026-05-24T00:00:00Z"));
        postRepository.saveAll(List.of(activePost, deletedPost));

        List<Post> posts = postRepository.findByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc("abc");

        assertThat(posts)
                .extracting(Post::getBody)
                .containsExactly("abc 表示する投稿");
    }

    @Test
    @DisplayName("投稿一覧_タグあり_findAllWithTagsは投稿に紐づくタグを含めて返す")
    void findAllWithTags_whenPostsHaveTags_returnsPostsWithTags() {
        Post post = postRepository.save(new Post(
                "alice", "#java #spring 本文です", Instant.parse("2026-05-23T01:00:00Z")));
        Tag java = tagRepository.save(new Tag("java"));
        Tag spring = tagRepository.save(new Tag("spring"));
        postTagRepository.saveAll(List.of(new PostTag(post, java), new PostTag(post, spring)));
        flushAndClear();

        List<Post> posts = postRepository.findAllWithTagsByIdIn(List.of(post.getId()));

        assertThat(posts).singleElement()
                .satisfies(actual -> assertThat(actual.getTags())
                        .extracting(Tag::getName)
                        .containsExactlyInAnyOrder("java", "spring"));
    }

    @Test
    @DisplayName("投稿検索_タグあり_findByKeywordWithTagsは検索結果に紐づくタグを含めて返す")
    void findByKeywordWithTags_whenPostsHaveTags_returnsMatchingPostsWithTags() {
        Post matchingPost = postRepository.save(new Post(
                "alice", "abc #java 本文です", Instant.parse("2026-05-23T01:00:00Z")));
        Post otherPost = postRepository.save(new Post(
                "bob", "対象外 #spring 本文です", Instant.parse("2026-05-23T02:00:00Z")));
        Tag java = tagRepository.save(new Tag("java"));
        Tag spring = tagRepository.save(new Tag("spring"));
        postTagRepository.saveAll(List.of(new PostTag(matchingPost, java), new PostTag(otherPost, spring)));
        flushAndClear();

        List<Post> posts = postRepository.findByKeywordWithTags("abc");

        assertThat(posts).singleElement()
                .satisfies(actual -> {
                    assertThat(actual.getBody()).isEqualTo("abc #java 本文です");
                    assertThat(actual.getTags())
                            .extracting(Tag::getName)
                            .containsExactly("java");
                });
    }

    @Test
    @DisplayName("投稿一覧_削除済み投稿_findAllWithTagsは投稿とタグを表示しない")
    void findAllWithTags_whenPostDeleted_excludesPostAndTags() {
        Post activePost = new Post("alice", "#java 表示する投稿", Instant.parse("2026-05-23T01:00:00Z"));
        Post deletedPost = new Post("bob", "#spring 表示しない投稿", Instant.parse("2026-05-23T02:00:00Z"));
        deletedPost.delete(Instant.parse("2026-05-24T00:00:00Z"));
        postRepository.saveAll(List.of(activePost, deletedPost));
        Tag java = tagRepository.save(new Tag("java"));
        Tag spring = tagRepository.save(new Tag("spring"));
        postTagRepository.saveAll(List.of(new PostTag(activePost, java), new PostTag(deletedPost, spring)));
        flushAndClear();

        List<Post> posts = postRepository.findAllWithTagsByIdIn(List.of(activePost.getId(), deletedPost.getId()));

        assertThat(posts)
                .extracting(Post::getBody)
                .containsExactly("#java 表示する投稿");
        assertThat(posts.get(0).getTags())
                .extracting(Tag::getName)
                .containsExactly("java");
    }

    @Test
    @DisplayName("投稿一覧API_JOIN行_削除済みを除外しタグといいね数を返す")
    void findApiRowsByIds_excludesDeletedPostsAndReturnsTagsAndLikes() {
        Post activePost = postRepository.save(new Post(
                "alice", "#java #spring 表示する投稿", "purple", Instant.parse("2026-05-23T03:00:00Z")));
        Post deletedPost = new Post(
                "bob", "#oracle 表示しない投稿", "green", Instant.parse("2026-05-23T04:00:00Z"));
        deletedPost.delete(Instant.parse("2026-05-24T00:00:00Z"));
        postRepository.save(deletedPost);
        Tag java = tagRepository.save(new Tag("java"));
        Tag spring = tagRepository.save(new Tag("spring"));
        Tag oracle = tagRepository.save(new Tag("oracle"));
        postTagRepository.saveAll(List.of(
                new PostTag(activePost, java),
                new PostTag(activePost, spring),
                new PostTag(deletedPost, oracle)
        ));
        postRepository.incrementLikesCountById(activePost.getId());
        postRepository.incrementLikesCountById(activePost.getId());
        postRepository.incrementLikesCountById(deletedPost.getId());
        flushAndClear();

        List<Long> ids = postRepository.findLatestIds(PageRequest.of(0, 50));
        List<PostApiRow> rows = postRepository.findApiRowsByIds(ids);

        assertThat(ids).containsExactly(activePost.getId());
        assertThat(rows)
                .extracting(PostApiRow::body)
                .containsOnly("#java #spring 表示する投稿");
        assertThat(rows)
                .extracting(PostApiRow::tagName)
                .containsExactly("java", "spring");
        assertThat(rows)
                .extracting(PostApiRow::likesCount)
                .containsOnly(2L);
    }

    @Test
    @DisplayName("いいね累積_存在する投稿_likesCountを押した回数だけ増やす")
    void incrementLikesCountById_whenPostExists_incrementsEveryTime() {
        Post post = postRepository.saveAndFlush(new Post(
                "alice", "本文です", Instant.parse("2026-05-23T01:00:00Z")));

        int first = postRepository.incrementLikesCountById(post.getId());
        int second = postRepository.incrementLikesCountById(post.getId());
        flushAndClear();

        assertThat(first).isEqualTo(1);
        assertThat(second).isEqualTo(1);
        assertThat(postRepository.findByIdAndDeletedAtIsNull(post.getId()))
                .get()
                .extracting(Post::getLikesCount)
                .isEqualTo(2L);
        assertThat(postRepository.findLikesCountById(post.getId())).contains(2L);
    }

    @Test
    @DisplayName("いいね累積_削除済み投稿_likesCountを増やさない")
    void incrementLikesCountById_whenPostDeleted_doesNotIncrement() {
        Post post = new Post("alice", "削除済み投稿", Instant.parse("2026-05-23T01:00:00Z"));
        post.delete(Instant.parse("2026-05-24T00:00:00Z"));
        Post savedPost = postRepository.saveAndFlush(post);

        int updatedRows = postRepository.incrementLikesCountById(savedPost.getId());
        flushAndClear();

        assertThat(updatedRows).isZero();
        assertThat(postRepository.findLikesCountById(savedPost.getId())).isEmpty();
    }

    @Test
    @DisplayName("投稿詳細_削除済み投稿_findByIdAndDeletedAtIsNullは空を返す")
    void findByIdAndDeletedAtIsNull_whenDeleted_returnsEmpty() {
        Post deletedPost = new Post("alice", "削除済み投稿", Instant.parse("2026-05-23T01:00:00Z"));
        deletedPost.delete(Instant.parse("2026-05-24T00:00:00Z"));
        Post savedPost = postRepository.saveAndFlush(deletedPost);

        Optional<Post> actual = postRepository.findByIdAndDeletedAtIsNull(savedPost.getId());

        assertThat(actual).isEmpty();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
