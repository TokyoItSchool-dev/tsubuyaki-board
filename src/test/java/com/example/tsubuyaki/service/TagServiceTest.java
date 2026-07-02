package com.example.tsubuyaki.service;

import com.example.tsubuyaki.repository.TagEntity;
import com.example.tsubuyaki.repository.TagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Test
    @DisplayName("タグ抽出_本文に複数タグがあるとき_タグ名だけを出現順で返す")
    void タグ抽出_本文に複数タグがあるとき_タグ名だけを出現順で返す() {
        TagService tagService = new TagService(tagRepository);

        List<String> actual = tagService.extractTagNames("""
                今日は #SpringBoot を勉強しました。
                #Java #Spring
                """);

        assertThat(actual).containsExactly("SpringBoot", "Java", "Spring");
    }

    @Test
    @DisplayName("タグ抽出_同じタグが複数あるとき_重複を除く")
    void タグ抽出_同じタグが複数あるとき_重複を除く() {
        TagService tagService = new TagService(tagRepository);

        List<String> actual = tagService.extractTagNames("#Java #Java #Java_21 #Java");

        assertThat(actual).containsExactly("Java", "Java_21");
    }

    @Test
    @DisplayName("タグ抽出_全角文字を含むとき_タグ名として返す")
    void タグ抽出_全角文字を含むとき_タグ名として返す() {
        TagService tagService = new TagService(tagRepository);

        List<String> actual = tagService.extractTagNames("今日は #社内勉強会 と #開発_2026 を共有します。 #朝会。");

        assertThat(actual).containsExactly("社内勉強会", "開発_2026", "朝会");
    }

    @Test
    @DisplayName("タグ登録_既存タグがあるとき_保存せず再利用する")
    void タグ登録_既存タグがあるとき_保存せず再利用する() {
        TagService tagService = new TagService(tagRepository);
        TagEntity existing = new TagEntity(1L, "Java");
        given(tagRepository.findByName("Java")).willReturn(Optional.of(existing));

        List<TagEntity> actual = tagService.resolveTags("#Java");

        assertThat(actual).containsExactly(existing);
        verify(tagRepository).findByName("Java");
    }

    @Test
    @DisplayName("タグ登録_タグなし本文_空リストを返す")
    void タグ登録_タグなし本文_空リストを返す() {
        TagService tagService = new TagService(tagRepository);

        List<TagEntity> actual = tagService.resolveTags("タグはありません");

        assertThat(actual).isEmpty();
    }
}
