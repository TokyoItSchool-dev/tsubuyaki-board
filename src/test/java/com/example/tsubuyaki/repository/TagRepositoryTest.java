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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class TagRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagRepository tagRepository;

    @Test
    @DisplayName("タグ保存_投稿とタグを関連付けて保存できる")
    void savePostWithTag_persistsRelationship() {
        Tag tag = tagRepository.save(new Tag("java"));
        Post post = new Post("alice", "本文 #java", Instant.parse("2026-07-02T10:00:00Z"));
        post.addTag(tag);

        Post savedPost = postRepository.save(post);

        Post foundPost = postRepository.findById(savedPost.getId()).orElseThrow();
        assertThat(foundPost.getTags()).extracting(Tag::getName).containsExactly("java");
    }

    @Test
    @DisplayName("タグ一覧_対象タグの投稿のみ新着順で取得し論理削除済みは除外する")
    void findByTagName_returnsActivePostsNewestFirst() {
        Tag tag = tagRepository.save(new Tag("java"));
        Post older = new Post("alice", "古い投稿 #java", Instant.parse("2026-07-02T09:00:00Z"));
        older.addTag(tag);
        Post newer = new Post("bob", "新しい投稿 #java", Instant.parse("2026-07-02T10:00:00Z"));
        newer.addTag(tag);
        Post deleted = new Post("carol", "削除済み #java", Instant.parse("2026-07-02T11:00:00Z"));
        deleted.addTag(tag);
        deleted.markDeleted(Instant.parse("2026-07-02T12:00:00Z"));
        postRepository.save(older);
        postRepository.save(newer);
        postRepository.save(deleted);

        List<Post> posts = postRepository.findTop50ByTagsNameAndDeletedAtIsNullOrderByCreatedAtDesc("java");

        assertThat(posts).extracting(Post::getBody).containsExactly("新しい投稿 #java", "古い投稿 #java");
    }
}
