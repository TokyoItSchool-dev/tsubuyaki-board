package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class PostRepositoryTest {

    // 実際の JPA Repository を使い、メソッド名クエリの結果を検証する。
    @Autowired
    private PostRepository postRepository;

    // タグ検索用に、本文から抽出されたタグを保存するRepository。
    @Autowired
    private TagRepository tagRepository;

    @Test
    @DisplayName("投稿一覧_51件以上の投稿がある場合_新着50件だけを返す")
    void 投稿一覧_51件以上の投稿がある場合_新着50件だけを返す() {
        // 51件の投稿を1秒ずつ作成日時をずらして用意する。
        LocalDateTime baseTime = LocalDateTime.of(2026, 5, 23, 10, 0);
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < 51; i++) {
            posts.add(new Post("user" + i, "body" + i, baseTime.plusSeconds(i)));
        }
        // 用意した投稿をDBへ保存し、Repository の検索対象にする。
        postRepository.saveAll(posts);

        // createdAt 降順で最新50件だけを取得する。
        List<Post> latestPosts = postRepository.findTop50ByDeletedAtOrderByCreatedAtDesc(Post.NOT_DELETED);

        // 取得件数が50件で、最も古い body0 が除外されていることを順序込みで確認する。
        assertThat(latestPosts).hasSize(50);
        assertThat(latestPosts).extracting(Post::getBody)
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

    @Test
    @DisplayName("投稿検索_qあり_本文に一致する投稿のみ新着順で返す")
    void 投稿検索_qあり_本文に一致する投稿のみ新着順で返す() {
        // 検索対象と非対象の投稿を作成日時をずらして保存する。
        postRepository.save(new Post("alice", "keywordを含む古い本文", LocalDateTime.of(2026, 5, 23, 10, 0)));
        postRepository.save(new Post("bob", "一致しない本文", LocalDateTime.of(2026, 5, 23, 10, 1)));
        postRepository.save(new Post("carol", "KEYWORDを含む新しい本文", LocalDateTime.of(2026, 5, 23, 10, 2)));

        // 大文字小文字を区別せず本文LIKE検索し、createdAt降順で取得する。
        List<Post> posts = postRepository.findTop50ByDeletedAtAndBodyContainingIgnoreCaseOrderByCreatedAtDesc(
                Post.NOT_DELETED, "keyword");

        // 本文に一致する投稿だけが新着順で返ることを確認する。
        assertThat(posts).extracting(Post::getBody)
                .containsExactly("KEYWORDを含む新しい本文", "keywordを含む古い本文");
    }

    @Test
    @DisplayName("投稿検索_qあり_一致しない場合は0件を返す")
    void 投稿検索_qあり_一致しない場合は0件を返す() {
        // 検索語を含まない投稿だけを保存する。
        postRepository.save(new Post("alice", "通常の本文", LocalDateTime.of(2026, 5, 23, 10, 0)));

        // 一致しないキーワードで検索する。
        List<Post> posts = postRepository.findTop50ByDeletedAtAndBodyContainingIgnoreCaseOrderByCreatedAtDesc(
                Post.NOT_DELETED, "missing");

        // 一致しない場合は空の一覧を返すことを確認する。
        assertThat(posts).isEmpty();
    }

    @Test
    @DisplayName("投稿検索_タグ名に一致する投稿を新着順で返す")
    void 投稿検索_タグ名に一致する投稿を新着順で返す() {
        // 本文には検索語を含めず、タグ名だけで一致する投稿を用意する。
        Post oldPost = postRepository.save(
                new Post("alice", "古いタグ投稿", LocalDateTime.of(2026, 5, 23, 10, 0)));
        Post otherPost = postRepository.save(
                new Post("bob", "別タグ投稿", LocalDateTime.of(2026, 5, 23, 10, 1)));
        Post newPost = postRepository.save(
                new Post("carol", "新しいタグ投稿", LocalDateTime.of(2026, 5, 23, 10, 2)));
        tagRepository.save(new Tag(oldPost, "spring"));
        tagRepository.save(new Tag(otherPost, "java"));
        tagRepository.save(new Tag(newPost, "spring"));

        // #spring 検索時はタグ名 spring に一致する投稿だけを createdAt 降順で取得する。
        List<Post> posts = postRepository.findByTagNameAndPostDeletedAtOrderByCreatedAtDesc(
                "spring", Post.NOT_DELETED, PageRequest.of(0, 50));

        assertThat(posts).extracting(Post::getBody)
                .containsExactly("新しいタグ投稿", "古いタグ投稿");
    }

    @Test
    @DisplayName("投稿作成_投稿者カラー_保存後に同じ色を取得できる")
    void 投稿作成_投稿者カラー_保存後に同じ色を取得できる() {
        // 投稿者カラーを指定した投稿を保存する。
        Post savedPost = postRepository.save(
                new Post("alice", "色付き本文", "#ef4444", LocalDateTime.of(2026, 5, 23, 10, 0)));

        // 保存した投稿を取得し、投稿者カラーがDBに保持されていることを確認する。
        Post foundPost = postRepository.findById(savedPost.getId()).orElseThrow();
        assertThat(foundPost.getAuthorColor()).isEqualTo("#ef4444");
    }
    @Test
    @DisplayName("投稿一覧_削除フラグ0_正常に表示対象として取得できる")
    void 投稿一覧_削除フラグ0_正常に表示対象として取得できる() {
        Post activePost = postRepository.save(
                new Post("alice", "表示される本文", LocalDateTime.of(2026, 5, 23, 10, 0)));

        List<Post> posts = postRepository.findTop50ByDeletedAtOrderByCreatedAtDesc(Post.NOT_DELETED);

        assertThat(posts).contains(activePost);
    }

    @Test
    @DisplayName("投稿一覧_削除フラグ1_表示対象から除外される")
    void 投稿一覧_削除フラグ1_表示対象から除外される() {
        Post deletedPost = new Post("alice", "削除済み本文", LocalDateTime.of(2026, 5, 23, 10, 0));
        deletedPost.markDeleted();
        postRepository.save(deletedPost);

        List<Post> posts = postRepository.findTop50ByDeletedAtOrderByCreatedAtDesc(Post.NOT_DELETED);

        assertThat(posts).doesNotContain(deletedPost);
    }

    @Test
    @DisplayName("投稿検索_削除フラグ1_本文検索とタグ検索から除外される")
    void 投稿検索_削除フラグ1_本文検索とタグ検索から除外される() {
        Post deletedPost = new Post("alice", "keyword #spring",
                LocalDateTime.of(2026, 5, 23, 10, 0));
        deletedPost.markDeleted();
        Post savedPost = postRepository.save(deletedPost);
        tagRepository.save(new Tag(savedPost, "spring"));

        List<Post> bodyPosts = postRepository
                .findTop50ByDeletedAtAndBodyContainingIgnoreCaseOrderByCreatedAtDesc(
                        Post.NOT_DELETED, "keyword");
        List<Post> tagPosts = postRepository.findByTagNameAndPostDeletedAtOrderByCreatedAtDesc(
                "spring", Post.NOT_DELETED, PageRequest.of(0, 50));

        assertThat(bodyPosts).doesNotContain(savedPost);
        assertThat(tagPosts).doesNotContain(savedPost);
    }
}
