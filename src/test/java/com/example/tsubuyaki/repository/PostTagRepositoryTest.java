package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostTag;
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
class PostTagRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private PostTagRepository postTagRepository;

    @Test
    @DisplayName("タグ一覧_タグ名指定_削除済み投稿を除外し新着順で返す")
    void findPostsByTagName_excludesDeletedPostsAndOrdersByCreatedAtDesc() {
        Post oldPost = postRepository.save(new Post("alice", "#java 古い投稿", Instant.parse("2026-05-23T01:00:00Z")));
        Post newPost = postRepository.save(new Post("bob", "#java 新しい投稿", Instant.parse("2026-05-23T03:00:00Z")));
        Post deletedPost = new Post("carol", "#java 削除済み投稿", Instant.parse("2026-05-23T04:00:00Z"));
        deletedPost.delete(Instant.parse("2026-05-24T00:00:00Z"));
        postRepository.save(deletedPost);
        Tag java = tagRepository.save(new Tag("java"));
        postTagRepository.saveAll(List.of(
                new PostTag(oldPost, java),
                new PostTag(newPost, java),
                new PostTag(deletedPost, java)
        ));

        List<Post> posts = postTagRepository.findPostsByTagName("java");

        assertThat(posts)
                .extracting(Post::getBody)
                .containsExactly("#java 新しい投稿", "#java 古い投稿");
    }

    @Test
    @DisplayName("タグ保存_同じタグ名_重複登録されない")
    void findByName_whenSameNameExists_reusesExistingTag() {
        Tag savedTag = tagRepository.saveAndFlush(new Tag("java"));

        assertThat(tagRepository.findByName("java"))
                .contains(savedTag);
    }
}
