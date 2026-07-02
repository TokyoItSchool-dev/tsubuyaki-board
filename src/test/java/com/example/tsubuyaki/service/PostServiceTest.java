package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.Tag;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.TagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("Service_latest_Repositoryの最新50件を返す")
    void latest_呼び出されたとき_Repositoryの最新50件を返す() {
        List<Post> repositoryResult = List.of(
                new Post("alice", "新しい投稿", Instant.parse("2026-06-26T10:00:00Z")),
                new Post("bob", "古い投稿", Instant.parse("2026-06-26T09:00:00Z")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(repositoryResult);

        List<Post> latestPosts = postService.latest();

        assertThat(latestPosts).isSameAs(repositoryResult);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("Service_search_キーワードあり_body検索結果を返す")
    void search_キーワードあり_body検索結果を返す() {
        List<Post> repositoryResult = List.of(
                new Post("alice", "検索フォーム", Instant.parse("2026-06-26T10:00:00Z")));
        given(postRepository.findTop50ByBodyContainingOrderByCreatedAtDesc("検索"))
                .willReturn(repositoryResult);

        List<Post> posts = postService.search(" 検索 ");

        assertThat(posts).isSameAs(repositoryResult);
        verify(postRepository).findTop50ByBodyContainingOrderByCreatedAtDesc("検索");
        verify(postRepository, never()).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("Service_search_空文字_通常一覧を返す")
    void search_空文字_通常一覧を返す() {
        List<Post> repositoryResult = List.of(
                new Post("alice", "通常一覧", Instant.parse("2026-06-26T10:00:00Z")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(repositoryResult);

        List<Post> posts = postService.search("   ");

        assertThat(posts).isSameAs(repositoryResult);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
        verify(postRepository, never()).findTop50ByBodyContainingOrderByCreatedAtDesc(any());
    }

    @Test
    @DisplayName("Service_create_投稿者と本文とアバター色_作成日時を付けて保存する")
    void create_投稿者と本文とアバター色_作成日時を付けて保存する() {
        given(postRepository.save(any(Post.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        Post savedPost = postService.create("alice", "M3 の投稿", "green");

        assertThat(savedPost.getAuthor()).isEqualTo("alice");
        assertThat(savedPost.getBody()).isEqualTo("M3 の投稿");
        assertThat(savedPost.getAvatarColor()).isEqualTo("green");
        assertThat(savedPost.getCreatedAt()).isNotNull();
        verify(postRepository).save(savedPost);
    }

    @Test
    @DisplayName("Service_create_本文のタグ_重複を除いて投稿に関連付ける")
    void create_本文のタグ_重複を除いて投稿に関連付ける() {
        Tag existingSpringTag = new Tag("spring");
        given(tagRepository.findByName("java")).willReturn(Optional.empty());
        given(tagRepository.findByName("spring")).willReturn(Optional.of(existingSpringTag));
        given(tagRepository.save(any(Tag.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

        Post savedPost = postService.create("alice", "#Java と #spring と #java", "green");

        assertThat(savedPost.getTags())
                .extracting(Tag::getName)
                .containsExactly("java", "spring");
        verify(tagRepository).save(any(Tag.class));
        verify(postRepository).save(savedPost);
    }

    @Test
    @DisplayName("Service_findById_id指定でRepositoryの投稿を返す")
    void findById_id指定でRepositoryの投稿を返す() {
        Post post = new Post("alice", "M4 の詳細投稿", Instant.parse("2026-06-26T11:00:00Z"));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Optional<Post> foundPost = postService.findById(1L);

        assertThat(foundPost).containsSame(post);
        verify(postRepository).findById(1L);
    }

    @Test
    @DisplayName("Service_findById_存在しないid_空を返す")
    void findById_存在しないid_空を返す() {
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        Optional<Post> foundPost = postService.findById(999L);

        assertThat(foundPost).isEmpty();
        verify(postRepository).findById(999L);
    }
}
