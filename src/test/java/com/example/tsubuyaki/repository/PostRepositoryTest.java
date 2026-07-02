package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
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
    @DisplayName("投稿一覧_51件以上ある場合_新着順で50件だけ取得する")
    void 投稿一覧_51件以上ある場合_新着順で50件だけ取得する() {
        LocalDateTime baseTime = LocalDateTime.parse("2026-05-23T00:00:00");
        IntStream.rangeClosed(0, 50)
                .mapToObj(index -> new Post("user-" + index, "body-" + index, baseTime.plusSeconds(index)))
                .forEach(postRepository::save);
        postRepository.flush();

        List<Post> posts = postRepository.findTop50ByOrderByCreatedAtDesc();

        assertThat(posts).hasSize(50);
        assertThat(posts.get(0).getAuthor()).isEqualTo("user-50");
        assertThat(posts.get(49).getAuthor()).isEqualTo("user-1");
        assertThat(posts).extracting(Post::getAuthor).doesNotContain("user-0");
    }

    @Test
    @DisplayName("投稿検索_本文にキーワードを含む投稿のみ新着順で取得する")
    void 投稿検索_本文にキーワードを含む投稿のみ新着順で取得する() {
        postRepository.save(new Post("alice", "朝会メモを共有しました", LocalDateTime.parse("2026-05-23T10:00:00")));
        postRepository.save(new Post("bob", "昼会メモを共有しました", LocalDateTime.parse("2026-05-23T11:00:00")));
        postRepository.save(new Post("carol", "雑談です", LocalDateTime.parse("2026-05-23T12:00:00")));
        postRepository.flush();

        List<Post> posts = postRepository.findByBodyContainingOrderByCreatedAtDesc("共有");

        assertThat(posts).extracting(Post::getAuthor).containsExactly("bob", "alice");
    }

    @Test
    @DisplayName("投稿作成_アバター色を保存して取得できる")
    void 投稿作成_アバター色を保存して取得できる() {
        Post saved = postRepository.saveAndFlush(
                new Post("alice", "今日の共有です", "Purple", LocalDateTime.parse("2026-05-23T10:00:00")));

        Post post = postRepository.findById(saved.getId()).orElseThrow();

        assertThat(post.getAvatarColor()).isEqualTo("Purple");
    }

    @Test
    @DisplayName("ハッシュタグ_タグ名に関連する投稿のみ新着順で取得する")
    void ハッシュタグ_タグ名に関連する投稿のみ新着順で取得する() {
        Tag java = tagRepository.save(new Tag("java"));
        Tag spring = tagRepository.save(new Tag("spring"));
        Post oldPost = new Post("alice", "#java の話です", LocalDateTime.parse("2026-05-23T10:00:00"));
        oldPost.replaceTags(List.of(java));
        Post newPost = new Post("bob", "#java #spring の話です", LocalDateTime.parse("2026-05-23T11:00:00"));
        newPost.replaceTags(List.of(java, spring));
        Post otherPost = new Post("carol", "#spring の話です", LocalDateTime.parse("2026-05-23T12:00:00"));
        otherPost.replaceTags(List.of(spring));
        postRepository.save(oldPost);
        postRepository.save(newPost);
        postRepository.save(otherPost);
        postRepository.flush();

        List<Post> posts = postRepository.findDistinctByTagsNameOrderByCreatedAtDesc("java");

        assertThat(posts).extracting(Post::getAuthor).containsExactly("bob", "alice");
        assertThat(tagRepository.countByName("java")).isEqualTo(1);
    }
    @Test
    @DisplayName("投稿一覧_11件ある場合_10件ごとにページが分かれる")
    void 投稿一覧_11件ある場合_10件ごとにページが分かれる() {
        LocalDateTime baseTime = LocalDateTime.parse("2026-05-23T00:00:00");
        IntStream.rangeClosed(0, 10)
                .mapToObj(index -> new Post("user-" + index, "body-" + index, baseTime.plusSeconds(index)))
                .forEach(postRepository::save);
        postRepository.flush();

        Page<Post> firstPage = postRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10));
        Page<Post> secondPage = postRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(1, 10));

        assertThat(firstPage.getContent()).hasSize(10);
        assertThat(secondPage.getContent()).hasSize(1);
        assertThat(firstPage.getContent()).extracting(Post::getAuthor).containsExactly(
                "user-10",
                "user-9",
                "user-8",
                "user-7",
                "user-6",
                "user-5",
                "user-4",
                "user-3",
                "user-2",
                "user-1");
        assertThat(secondPage.getContent()).extracting(Post::getAuthor).containsExactly("user-0");
    }
}
