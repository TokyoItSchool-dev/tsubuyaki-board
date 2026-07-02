package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

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
    @DisplayName("投稿一覧_51件以上あるとき_新着50件を新着順で返す")
    void findTop50ByOrderByCreatedAtDesc_over51_returnsLatest50Only() {
        LocalDateTime base = LocalDateTime.parse("2026-05-23T00:00:00");
        for (int i = 1; i <= 51; i++) {
            postRepository.save(new Post("user-" + i, "body-" + i, base.plusSeconds(i)));
        }

        List<Post> posts = postRepository.findTop50ByOrderByCreatedAtDesc();

        assertThat(posts).hasSize(50);
        assertThat(posts.get(0).getAuthor()).isEqualTo("user-51");
        assertThat(posts.get(49).getAuthor()).isEqualTo("user-2");
        assertThat(posts).extracting(Post::getAuthor).doesNotContain("user-1");
    }

    @Test
    @DisplayName("投稿検索_q指定_bodyの部分一致だけを新着順で最大50件返す")
    void findTop50ByBodyContainingOrderByCreatedAtDesc_keyword_returnsBodyMatchesOnlyLatest50() {
        LocalDateTime base = LocalDateTime.parse("2026-05-23T00:00:00");
        postRepository.save(new Post("検索さん", "本文は一致しません", base.plusSeconds(1)));
        for (int i = 1; i <= 51; i++) {
            postRepository.save(new Post("user-" + i, "検索対象の本文-" + i, base.plusSeconds(i + 1)));
        }

        List<Post> posts = postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("検索対象");

        assertThat(posts).hasSize(50);
        assertThat(posts.get(0).getBody()).isEqualTo("検索対象の本文-51");
        assertThat(posts.get(49).getBody()).isEqualTo("検索対象の本文-2");
        assertThat(posts).extracting(Post::getAuthor).doesNotContain("検索さん");
        assertThat(posts).extracting(Post::getBody).doesNotContain("検索対象の本文-1");
    }

    @Test
    @DisplayName("投稿作成_avatarColor選択_投稿と一緒に保存される")
    void save_withAvatarColor_persistsAvatarColor() {
        Post saved = postRepository.save(new Post(
                "tanaka",
                "アバター色を保存します",
                LocalDateTime.parse("2026-05-23T09:00:00"),
                "green"));

        Post found = postRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getAvatarColor()).isEqualTo("green");
    }

    @Test
    @DisplayName("タグ別投稿一覧_タグ名指定_紐づく投稿だけを新着順で返す")
    void findTop50ByTagsNameOrderByCreatedAtDesc_tagName_returnsTaggedPostsOnly() {
        Tag java = tagRepository.save(new Tag("java"));
        Post oldTagged = new Post("alice", "#java old", LocalDateTime.parse("2026-05-23T09:00:00"));
        oldTagged.addTag(java);
        Post newTagged = new Post("bob", "#java new", LocalDateTime.parse("2026-05-23T10:00:00"));
        newTagged.addTag(java);
        postRepository.save(new Post("carol", "no tag", LocalDateTime.parse("2026-05-23T11:00:00")));
        postRepository.save(oldTagged);
        postRepository.save(newTagged);

        List<Post> posts = postRepository.findTop50ByTagsNameOrderByCreatedAtDesc("java");

        assertThat(posts)
                .extracting(Post::getAuthor)
                .containsExactly("bob", "alice");
    }
}
