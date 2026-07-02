package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMostOnce;
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
    @DisplayName("投稿一覧_取得するとき_Repositoryから新着50件を取得する")
    void 投稿一覧_取得するとき_Repositoryから新着50件を取得する() {
        List<Post> posts = List.of(
                new Post("alice", "new", LocalDateTime.parse("2026-05-23T10:00:00")),
                new Post("bob", "old", LocalDateTime.parse("2026-05-23T09:00:00")));
        given(postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc()).willReturn(posts);

        List<Post> latestPosts = postService.latest();

        assertThat(latestPosts).isEqualTo(posts);
        verify(postRepository).findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿検索_q未指定_新着50件を取得する")
    void 投稿検索_q未指定_新着50件を取得する() {
        List<Post> posts = List.of(new Post("alice", "new", LocalDateTime.parse("2026-05-23T10:00:00")));
        given(postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc()).willReturn(posts);

        List<Post> foundPosts = postService.search(null);

        assertThat(foundPosts).isEqualTo(posts);
        verify(postRepository).findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿検索_q空文字_新着50件を取得する")
    void 投稿検索_q空文字_新着50件を取得する() {
        List<Post> posts = List.of(new Post("alice", "new", LocalDateTime.parse("2026-05-23T10:00:00")));
        given(postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc()).willReturn(posts);

        List<Post> foundPosts = postService.search("   ");

        assertThat(foundPosts).isEqualTo(posts);
        verify(postRepository).findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿検索_q指定_body部分一致を新着順で最大50件取得する")
    void 投稿検索_q指定_body部分一致を新着順で最大50件取得する() {
        List<Post> posts = List.of(new Post("alice", "研修メモ", LocalDateTime.parse("2026-05-23T10:00:00")));
        given(postRepository.findTop50ByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc("研修"))
                .willReturn(posts);

        List<Post> foundPosts = postService.search("  研修  ");

        assertThat(foundPosts).isEqualTo(posts);
        verify(postRepository).findTop50ByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDesc("研修");
    }

    @Test
    @DisplayName("投稿作成_正常入力_前後空白を除去してRepositoryに保存する")
    void 投稿作成_正常入力_前後空白を除去してRepositoryに保存する() {
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);

        postService.create("  tanaka  ", "  投稿本文です  ", "blue");

        verify(postRepository).save(captor.capture());
        Post savedPost = captor.getValue();
        assertThat(savedPost.getAuthor()).isEqualTo("tanaka");
        assertThat(savedPost.getBody()).isEqualTo("投稿本文です");
        assertThat(savedPost.getAvatarColor()).isEqualTo("blue");
        assertThat(savedPost.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("投稿作成_avatarColor未選択_既定色grayでRepositoryに保存する")
    void 投稿作成_avatarColor未選択_既定色grayでRepositoryに保存する() {
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);

        postService.create("tanaka", "投稿本文です", "   ");

        verify(postRepository).save(captor.capture());
        assertThat(captor.getValue().getAvatarColor()).isEqualTo("gray");
    }

    @Test
    @DisplayName("投稿作成_本文にハッシュタグあり_小文字化して投稿へ紐づける")
    void 投稿作成_本文にハッシュタグあり_小文字化して投稿へ紐づける() {
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        given(tagRepository.findByName("java")).willReturn(Optional.empty());
        given(tagRepository.findByName("spring_3")).willReturn(Optional.empty());
        given(tagRepository.save(new Tag("java"))).willReturn(new Tag("java"));
        given(tagRepository.save(new Tag("spring_3"))).willReturn(new Tag("spring_3"));

        postService.create("tanaka", "#Java と #spring_3 を学ぶ", "blue");

        verify(postRepository).save(captor.capture());
        assertThat(captor.getValue().getTags())
                .extracting(Tag::getName)
                .containsExactly("java", "spring_3");
    }

    @Test
    @DisplayName("投稿作成_同一タグが複数回出る_タグを重複登録しない")
    void 投稿作成_同一タグが複数回出る_タグを重複登録しない() {
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        given(tagRepository.findByName("java")).willReturn(Optional.of(new Tag("java")));

        postService.create("tanaka", "#Java #java #JAVA", "blue");

        verify(tagRepository, atMostOnce()).findByName("java");
        verify(postRepository).save(captor.capture());
        assertThat(captor.getValue().getTags())
                .extracting(Tag::getName)
                .containsExactly("java");
    }

    @Test
    @DisplayName("投稿作成_不正タグあり_英数字アンダースコア以外はタグにしない")
    void 投稿作成_不正タグあり_英数字アンダースコア以外はタグにしない() {
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        given(tagRepository.findByName("ok_1")).willReturn(Optional.empty());
        given(tagRepository.save(new Tag("ok_1"))).willReturn(new Tag("ok_1"));

        postService.create("tanaka", "# #! #ok_1 #日本語", "blue");

        verify(postRepository).save(captor.capture());
        assertThat(captor.getValue().getTags())
                .extracting(Tag::getName)
                .containsExactly("ok_1");
    }

    @Test
    @DisplayName("投稿詳細_取得するとき_RepositoryからIDで検索する")
    void 投稿詳細_取得するとき_RepositoryからIDで検索する() {
        Post post = new Post("tanaka", "本文", LocalDateTime.parse("2026-05-23T09:00:00"));
        given(postRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(post));

        Optional<Post> foundPost = postService.findById(1L);

        assertThat(foundPost).contains(post);
        verify(postRepository).findByIdAndDeletedAtIsNull(1L);
    }

    @Test
    @DisplayName("タグ別投稿一覧_タグ名指定_小文字化してRepositoryから取得する")
    void タグ別投稿一覧_タグ名指定_小文字化してRepositoryから取得する() {
        List<Post> posts = List.of(new Post("alice", "#java", LocalDateTime.parse("2026-05-23T10:00:00")));
        given(postRepository.findTop50ByDeletedAtIsNullAndTagsNameOrderByCreatedAtDesc("java")).willReturn(posts);

        List<Post> foundPosts = postService.findByTag(" Java ");

        assertThat(foundPosts).isEqualTo(posts);
        verify(postRepository).findTop50ByDeletedAtIsNullAndTagsNameOrderByCreatedAtDesc("java");
    }

    @Test
    @DisplayName("投稿削除_存在するID_deletedAtを設定し物理削除しない")
    void 投稿削除_存在するID_deletedAtを設定し物理削除しない() {
        Post post = new Post("tanaka", "削除対象", LocalDateTime.parse("2026-05-23T09:00:00"));
        given(postRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(post));

        boolean deleted = postService.delete(1L);

        assertThat(deleted).isTrue();
        assertThat(post.getDeletedAt()).isNotNull();
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("投稿削除_存在しないID_falseを返す")
    void 投稿削除_存在しないID_falseを返す() {
        given(postRepository.findByIdAndDeletedAtIsNull(999L)).willReturn(Optional.empty());

        boolean deleted = postService.delete(999L);

        assertThat(deleted).isFalse();
        verify(postRepository, never()).delete(any(Post.class));
    }
}
