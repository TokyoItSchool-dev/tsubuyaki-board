package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Tag;
import com.example.tsubuyaki.repository.TagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-01T04:30:00Z"), ZoneOffset.UTC);

    @Test
    @DisplayName("タグ登録_本文に複数タグがある場合_タグを抽出して保存する")
    void タグ登録_本文に複数タグがある場合_タグを抽出して保存する() {
        TagService tagService = new TagService(tagRepository, clock);

        tagService.createForPost(1L, "本文 #Java\n#AI です #研修");

        ArgumentCaptor<Tag> captor = ArgumentCaptor.forClass(Tag.class);
        then(tagRepository).should(org.mockito.Mockito.times(3)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(Tag::getPostId).containsOnly(1L);
        assertThat(captor.getAllValues()).extracting(Tag::getTagName).containsExactly("#Java", "#AI", "#研修");
        assertThat(captor.getAllValues()).extracting(Tag::getCreatedAt)
                .containsOnly(Instant.parse("2026-07-01T04:30:00Z"));
    }

    @Test
    @DisplayName("タグ登録_同じタグが複数回ある場合_重複を除いて保存する")
    void タグ登録_同じタグが複数回ある場合_重複を除いて保存する() {
        TagService tagService = new TagService(tagRepository, clock);

        tagService.createForPost(1L, "#Java #Java");

        ArgumentCaptor<Tag> captor = ArgumentCaptor.forClass(Tag.class);
        then(tagRepository).should().save(captor.capture());
        assertThat(captor.getValue().getTagName()).isEqualTo("#Java");
    }

    @Test
    @DisplayName("タグ表示_投稿一覧がある場合_投稿IDごとのタグ一覧を返す")
    void タグ表示_投稿一覧がある場合_投稿IDごとのタグ一覧を返す() {
        TagService tagService = new TagService(tagRepository, clock);
        Tag tag = new Tag(1L, "#Java", Instant.parse("2026-05-23T10:01:00Z"));
        given(tagRepository.findByPostIdInOrderByCreatedAtAscIdAsc(List.of(1L))).willReturn(List.of(tag));

        var actual = tagService.tagsByPostIds(List.of(1L));

        assertThat(actual).containsEntry(1L, List.of(tag));
    }

    @Test
    @DisplayName("タグ検索_パス名指定_先頭にシャープを付けたタグ名で投稿IDを検索する")
    void タグ検索_パス名指定_先頭にシャープを付けたタグ名で投稿IDを検索する() {
        TagService tagService = new TagService(tagRepository, clock);
        given(tagRepository.findPostIdsByTagNameOrderByPostCreatedAtDesc("#Java")).willReturn(List.of(2L, 1L));

        var actual = tagService.postIdsByPathName("Java");

        assertThat(actual).containsExactly(2L, 1L);
    }

    @Test
    @DisplayName("本文表示_本文にタグがある場合_タグ部分だけタグセグメントにする")
    void 本文表示_本文にタグがある場合_タグ部分だけタグセグメントにする() {
        TagService tagService = new TagService(tagRepository, clock);

        var actual = tagService.bodySegments("本文 #Java\n#AI です");

        assertThat(actual).extracting(TagTextSegment::getText)
                .containsExactly("本文 ", "#Java", "\n", "#AI", " です");
        assertThat(actual).extracting(TagTextSegment::isTag)
                .containsExactly(false, true, false, true, false);
    }
}
