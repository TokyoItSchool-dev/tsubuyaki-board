package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.domain.Tag;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.TagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
    @DisplayName("投稿一覧_latest_Repositoryから新着50件を取得する")
    void 投稿一覧_latest_Repositoryから新着50件を取得する() {
        List<Post> expected = List.of(new Post("alice", "hello", LocalDateTime.parse("2026-05-23T10:00:00")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(expected);

        List<Post> actual = postService.latest();

        assertThat(actual).isSameAs(expected);
    }

    @Test
    @DisplayName("投稿検索_searchByBody_Repositoryで本文部分一致検索する")
    void 投稿検索_searchByBody_Repositoryで本文部分一致検索する() {
        List<Post> expected = List.of(new Post("alice", "hello world", LocalDateTime.parse("2026-05-23T10:00:00")));
        given(postRepository.findByBodyContainingOrderByCreatedAtDesc("hello")).willReturn(expected);

        List<Post> actual = postService.searchByBody("hello");

        assertThat(actual).isSameAs(expected);
    }

    @Test
    @DisplayName("投稿詳細_findById_Repositoryから指定idの投稿を取得する")
    void 投稿詳細_findById_Repositoryから指定idの投稿を取得する() {
        Optional<Post> expected = Optional.of(new Post("alice", "hello", LocalDateTime.parse("2026-05-23T10:00:00")));
        given(postRepository.findById(1L)).willReturn(expected);

        Optional<Post> actual = postService.findById(1L);

        assertThat(actual).isSameAs(expected);
    }

    @Test
    @DisplayName("投稿作成_create_Repositoryに投稿者と本文とアバター色と作成日時を保存する")
    void 投稿作成_create_Repositoryに投稿者と本文とアバター色と作成日時を保存する() {
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);

        postService.create("alice", "今日の共有です", "Orange");

        verify(postRepository).save(captor.capture());
        Post saved = captor.getValue();
        assertThat(saved.getAuthor()).isEqualTo("alice");
        assertThat(saved.getBody()).isEqualTo("今日の共有です");
        assertThat(saved.getAvatarColor()).isEqualTo("Orange");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("ハッシュタグ_投稿作成_javaとspringを含む本文からタグを保存する")
    void ハッシュタグ_投稿作成_javaとspringを含む本文からタグを保存する() {
        given(tagRepository.findByName("java")).willReturn(Optional.empty());
        given(tagRepository.findByName("spring")).willReturn(Optional.empty());
        given(tagRepository.save(any(Tag.class))).willAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);

        postService.create("alice", "#java #spring の話です", "Blue");

        verify(postRepository).save(captor.capture());
        assertThat(captor.getValue().getTags()).extracting(Tag::getName).containsExactly("java", "spring");
    }

    @Test
    @DisplayName("ハッシュタグ_同じタグを含む別投稿_既存タグを利用し重複作成しない")
    void ハッシュタグ_同じタグを含む別投稿_既存タグを利用し重複作成しない() {
        Tag java = new Tag("java");
        given(tagRepository.findByName("java")).willReturn(Optional.of(java));
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);

        postService.create("alice", "#java の話です", "Blue");

        verify(tagRepository, never()).save(any(Tag.class));
        verify(postRepository).save(captor.capture());
        assertThat(captor.getValue().getTags()).containsExactly(java);
    }

    @Test
    @DisplayName("ハッシュタグ_タグを含まない投稿_タグを作成しない")
    void ハッシュタグ_タグを含まない投稿_タグを作成しない() {
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);

        postService.create("alice", "タグなし本文です", "Blue");

        verify(tagRepository, never()).save(any(Tag.class));
        verify(postRepository).save(captor.capture());
        assertThat(captor.getValue().getTags()).isEmpty();
    }

    @Test
    @DisplayName("ハッシュタグ_投稿更新_タグの追加削除を反映する")
    void ハッシュタグ_投稿更新_タグの追加削除を反映する() {
        Post post = new Post("alice", "#java の話です", "Blue", LocalDateTime.parse("2026-05-23T10:00:00"));
        post.replaceTags(List.of(new Tag("java")));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(tagRepository.findByName("spring")).willReturn(Optional.empty());
        given(tagRepository.save(any(Tag.class))).willAnswer(invocation -> invocation.getArgument(0));

        boolean updated = postService.update(1L, "alice", "#spring の話です", "Green");

        assertThat(updated).isTrue();
        assertThat(post.getBody()).isEqualTo("#spring の話です");
        assertThat(post.getAvatarColor()).isEqualTo("Green");
        assertThat(post.getTags()).extracting(Tag::getName).containsExactly("spring");
    }

    @Test
    @DisplayName("ハッシュタグ_タグ一覧_findByTag_タグ名を小文字にしてRepositoryへ渡す")
    void ハッシュタグ_タグ一覧_findByTag_タグ名を小文字にしてRepositoryへ渡す() {
        List<Post> expected = List.of(new Post("alice", "#java", LocalDateTime.parse("2026-05-23T10:00:00")));
        given(postRepository.findDistinctByTagsNameOrderByCreatedAtDesc("java")).willReturn(expected);

        List<Post> actual = postService.findByTag("Java");

        assertThat(actual).isSameAs(expected);
    }

    @Test
    @DisplayName("いいね_初回_clientHashが未登録ならいいねを保存する")
    void いいね_初回_clientHashが未登録ならいいねを保存する() {
        Post post = new Post("alice", "hello", LocalDateTime.parse("2026-05-23T10:00:00"));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postLikeRepository.findByPostIdAndClientHash(1L, "abcdef12")).willReturn(Optional.empty());
        ArgumentCaptor<PostLike> captor = ArgumentCaptor.forClass(PostLike.class);

        boolean toggled = postService.toggleLike(1L, "abcdef12");

        assertThat(toggled).isTrue();
        verify(postLikeRepository).save(captor.capture());
        assertThat(captor.getValue().getPost()).isSameAs(post);
        assertThat(captor.getValue().getClientHash()).isEqualTo("abcdef12");
    }

    @Test
    @DisplayName("いいね_同じclientHashで再押下_登録済みいいねを削除する")
    void いいね_同じclientHashで再押下_登録済みいいねを削除する() {
        Post post = new Post("alice", "hello", LocalDateTime.parse("2026-05-23T10:00:00"));
        PostLike existingLike = new PostLike(post, "abcdef12");
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postLikeRepository.findByPostIdAndClientHash(1L, "abcdef12")).willReturn(Optional.of(existingLike));

        boolean toggled = postService.toggleLike(1L, "abcdef12");

        assertThat(toggled).isTrue();
        verify(postLikeRepository).delete(existingLike);
        verify(postLikeRepository, never()).save(any(PostLike.class));
    }

    @Test
    @DisplayName("いいね_異なるclientHash_それぞれ独立して登録できる")
    void いいね_異なるclientHash_それぞれ独立して登録できる() {
        Post post = new Post("alice", "hello", LocalDateTime.parse("2026-05-23T10:00:00"));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postLikeRepository.findByPostIdAndClientHash(1L, "abcdef12")).willReturn(Optional.empty());
        given(postLikeRepository.findByPostIdAndClientHash(1L, "12345678")).willReturn(Optional.empty());

        postService.toggleLike(1L, "abcdef12");
        postService.toggleLike(1L, "12345678");

        verify(postLikeRepository, times(2)).save(any(PostLike.class));
        verify(postLikeRepository).findByPostIdAndClientHash(1L, "abcdef12");
        verify(postLikeRepository).findByPostIdAndClientHash(1L, "12345678");
    }

    @Test
    @DisplayName("いいね_存在しない投稿id_いいねを保存せずfalseを返す")
    void いいね_存在しない投稿id_いいねを保存せずfalseを返す() {
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        boolean toggled = postService.toggleLike(999L, "abcdef12");

        assertThat(toggled).isFalse();
        verify(postLikeRepository, never()).save(any(PostLike.class));
    }
    @Test
    @DisplayName("投稿一覧_latestPage_Repositoryから指定ページを取得する")
    void 投稿一覧_latestPage_Repositoryから指定ページを取得する() {
        Page<Post> expected = new PageImpl<>(List.of(
                new Post("alice", "hello", LocalDateTime.parse("2026-05-23T10:00:00"))));
        given(postRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(1, 10))).willReturn(expected);

        Page<Post> actual = postService.latestPage(1, 10);

        assertThat(actual).isSameAs(expected);
    }

    @Test
    @DisplayName("投稿検索_searchByBodyPage_Repositoryで指定ページを検索する")
    void 投稿検索_searchByBodyPage_Repositoryで指定ページを検索する() {
        Page<Post> expected = new PageImpl<>(List.of(
                new Post("alice", "hello world", LocalDateTime.parse("2026-05-23T10:00:00"))));
        given(postRepository.findByBodyContainingOrderByCreatedAtDesc("hello", PageRequest.of(0, 10)))
                .willReturn(expected);

        Page<Post> actual = postService.searchByBodyPage("hello", 0, 10);

        assertThat(actual).isSameAs(expected);
    }

    @Test
    @DisplayName("ハッシュタグ_findByTagPage_タグ名を小文字にして指定ページを取得する")
    void ハッシュタグ_findByTagPage_タグ名を小文字にして指定ページを取得する() {
        Page<Post> expected = new PageImpl<>(List.of(
                new Post("alice", "#java", LocalDateTime.parse("2026-05-23T10:00:00"))));
        given(postRepository.findDistinctByTagsNameOrderByCreatedAtDesc("java", PageRequest.of(0, 10)))
                .willReturn(expected);

        Page<Post> actual = postService.findByTagPage("Java", 0, 10);

        assertThat(actual).isSameAs(expected);
    }
}
