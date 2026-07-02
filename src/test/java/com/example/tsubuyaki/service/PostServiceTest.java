package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.Tag;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.TagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_取得するとき_Repositoryの新着50件取得を使う")
    void findLatest50_取得するとき_Repositoryの新着50件取得を使う() {
        List<Post> expected = List.of(new Post(
                "alice",
                "hello",
                LocalDateTime.parse("2026-06-30T10:00:00")));
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(expected);

        List<Post> actual = postService.findLatest50();

        assertThat(actual).isSameAs(expected);
        verify(postRepository).findTop50ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿詳細_IDを指定したとき_Repositoryの未削除findByIdを使う")
    void findById_IDを指定したとき_Repositoryの未削除findByIdを使う() {
        Optional<Post> expected = Optional.of(new Post(
                "alice",
                "詳細表示の本文です",
                LocalDateTime.parse("2026-06-30T10:00:00")));
        given(postRepository.findByIdAndDeletedAtIsNull(10L)).willReturn(expected);

        Optional<Post> actual = postService.findById(10L);

        assertThat(actual).isSameAs(expected);
        verify(postRepository).findByIdAndDeletedAtIsNull(10L);
    }

    @Test
    @DisplayName("いいね_未登録のとき_いいねを登録する")
    void toggleLike_未登録のとき_いいねを登録する() {
        postService.toggleLike(10L, "abc12345");

        assertThat(postService.countLikes(10L)).isEqualTo(1L);
    }

    @Test
    @DisplayName("いいね_登録済みのとき_いいねを削除する")
    void toggleLike_登録済みのとき_いいねを削除する() {
        postService.toggleLike(10L, "abc12345");
        postService.toggleLike(10L, "abc12345");

        assertThat(postService.countLikes(10L)).isZero();
    }

    @Test
    @DisplayName("いいね_別ユーザーが押したとき_件数が増える")
    void toggleLike_別ユーザーが押したとき_件数が増える() {
        postService.toggleLike(10L, "abc12345");
        postService.toggleLike(10L, "def67890");

        assertThat(postService.countLikes(10L)).isEqualTo(2L);
    }

    @Test
    @DisplayName("投稿登録_投稿者と本文とアバター色を受け取ったとき_現在時刻付きの投稿を保存する")
    void create_投稿者と本文とアバター色を受け取ったとき_現在時刻付きの投稿を保存する() {
        LocalDateTime before = LocalDateTime.now();

        postService.create("alice", "本日の共有です", "green");

        LocalDateTime after = LocalDateTime.now();
        verify(postRepository).save(argThat(post ->
                "alice".equals(post.getAuthor())
                        && "本日の共有です".equals(post.getBody())
                        && "green".equals(post.getAvatarColor())
                        && !post.getCreatedAt().isBefore(before)
                        && !post.getCreatedAt().isAfter(after)));
    }

    @Test
    @DisplayName("投稿登録_本文にタグがあるとき_タグを正規化して投稿に紐づける")
    void create_本文にタグがあるとき_タグを正規化して投稿に紐づける() {
        Tag spring = new Tag("spring", LocalDateTime.parse("2026-06-30T00:00:00"));
        given(tagRepository.findByName("java")).willReturn(Optional.empty());
        given(tagRepository.findByName("spring")).willReturn(Optional.of(spring));

        postService.create("alice", "共有です #Java #spring #java", "blue");

        verify(postRepository).save(argThat(post ->
                post.getTags().stream().map(Tag::getName).collect(Collectors.toSet())
                        .equals(Set.of("java", "spring"))));
        verify(tagRepository).findByName("java");
        verify(tagRepository).findByName("spring");
    }

    @Test
    @DisplayName("投稿登録_タグ入力欄があるとき_本文タグと合わせて投稿に紐づける")
    void create_タグ入力欄があるとき_本文タグと合わせて投稿に紐づける() {
        given(tagRepository.findByName("java")).willReturn(Optional.empty());
        given(tagRepository.findByName("spring")).willReturn(Optional.empty());

        postService.create("alice", "共有です #Java", "blue", "spring java");

        verify(postRepository).save(argThat(post ->
                post.getTags().stream().map(Tag::getName).collect(Collectors.toSet())
                        .equals(Set.of("java", "spring"))));
        verify(tagRepository).findByName("java");
        verify(tagRepository).findByName("spring");
    }

    @Test
    @DisplayName("投稿削除_存在する未削除投稿のとき_deletedAtを設定する")
    void delete_存在する未削除投稿のとき_deletedAtを設定する() {
        Post post = new Post(
                "alice",
                "削除対象の投稿です",
                LocalDateTime.parse("2026-06-30T10:00:00"));
        given(postRepository.findByIdAndDeletedAtIsNull(10L)).willReturn(Optional.of(post));
        LocalDateTime before = LocalDateTime.now();

        postService.delete(10L);

        LocalDateTime after = LocalDateTime.now();
        assertThat(post.getDeletedAt()).isNotNull();
        assertThat(post.getDeletedAt()).isBetween(before, after);
        verify(postRepository).findByIdAndDeletedAtIsNull(10L);
    }
}
