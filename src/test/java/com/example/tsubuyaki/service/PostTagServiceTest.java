package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostReply;
import com.example.tsubuyaki.domain.Tag;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.PostReplyRepository;
import com.example.tsubuyaki.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class PostTagServiceTest {

    private PostRepository postRepository;

    private PostReplyRepository postReplyRepository;

    private TagRepository tagRepository;

    private PostService postService;

    @BeforeEach
    void setUp() {
        postRepository = mock(PostRepository.class);
        PostLikeRepository postLikeRepository = mock(PostLikeRepository.class);
        postReplyRepository = mock(PostReplyRepository.class);
        tagRepository = mock(TagRepository.class);
        postService = new PostService(
                postRepository,
                postLikeRepository,
                postReplyRepository,
                tagRepository,
                new TagParser()
        );
    }

    @Test
    @DisplayName("投稿作成_本文中のタグをTagテーブルへ保存し投稿に関連付ける")
    void create_whenBodyHasTags_savesTagsAndRelatesToPost() {
        Tag existingTag = new Tag("java");
        given(tagRepository.findByName("java")).willReturn(Optional.of(existingTag));
        given(tagRepository.findByName("spring")).willReturn(Optional.empty());
        given(tagRepository.save(any(Tag.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

        Post post = postService.create("alice", "#java と #spring と #java", "blue");

        assertThat(post.getTags()).extracting(Tag::getName).containsExactlyInAnyOrder("java", "spring");
        verify(tagRepository).save(argThat(tag -> "spring".equals(tag.getName())));
        verify(postRepository).save(post);
    }

    @Test
    @DisplayName("返信投稿_本文中にタグがあってもタグは生成保存しない")
    void createReply_whenBodyHasHashTag_doesNotCreateTags() {
        Post post = new Post("alice", "投稿本文", Instant.parse("2026-07-02T10:00:00Z"));
        given(postRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(post));

        boolean created = postService.createReply(1L, "bob", "返信 #java", "green");

        assertThat(created).isTrue();
        verify(postReplyRepository).save(any(PostReply.class));
        verifyNoInteractions(tagRepository);
    }

    @Test
    @DisplayName("タグ一覧_指定タグの投稿をRepositoryから取得する")
    void postsByTag_returnsPostsFromRepository() {
        List<Post> expectedPosts = List.of(
                new Post("alice", "本文 #java", Instant.parse("2026-07-02T10:00:00Z"))
        );
        given(postRepository.findTop50ByTagsNameAndDeletedAtIsNullOrderByCreatedAtDesc("java"))
                .willReturn(expectedPosts);

        List<Post> posts = postService.postsByTag("java");

        assertThat(posts).isSameAs(expectedPosts);
        verify(postRepository).findTop50ByTagsNameAndDeletedAtIsNullOrderByCreatedAtDesc("java");
        verify(tagRepository, never()).save(any(Tag.class));
    }
}
