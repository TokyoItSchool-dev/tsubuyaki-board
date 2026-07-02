package com.example.tsubuyaki.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class TagTest {

    @Test
    @DisplayName("Tag_生成時_タグ名を保持する")
    void constructor_keepsValues() {
        Tag tag = new Tag("java");

        assertThat(tag.getId()).isNull();
        assertThat(tag.getName()).isEqualTo("java");
    }

    @Test
    @DisplayName("Tag_equals_同一インスタンス_trueを返す")
    void equals_whenSameInstance_returnsTrue() {
        Tag tag = new Tag("java");

        assertThat(tag).isEqualTo(tag);
    }

    @Test
    @DisplayName("Tag_equals_型が異なる場合_falseを返す")
    void equals_whenOtherType_returnsFalse() {
        Tag tag = new Tag("java");

        assertThat(tag).isNotEqualTo("tag");
    }

    @Test
    @DisplayName("Tag_equals_ID未採番同士_別タグとして扱う")
    void equals_whenBothIdsAreNull_returnsFalse() {
        Tag tag = new Tag("java");
        Tag other = new Tag("java");

        assertThat(tag).isNotEqualTo(other);
    }

    @Test
    @DisplayName("Tag_equals_片方だけID採番済み_別タグとして扱う")
    void equals_whenEitherIdIsNull_returnsFalse() {
        Tag assignedId = new Tag("java");
        Tag nullId = new Tag("spring");
        ReflectionTestUtils.setField(assignedId, "id", 1L);

        assertThat(assignedId).isNotEqualTo(nullId);
        assertThat(nullId).isNotEqualTo(assignedId);
    }

    @Test
    @DisplayName("Tag_equals_ID採番済み同士_IDで同値判定する")
    void equals_whenBothIdsAreAssigned_comparesId() {
        Tag tag = new Tag("java");
        Tag sameId = new Tag("spring");
        Tag otherId = new Tag("oracle");
        ReflectionTestUtils.setField(tag, "id", 1L);
        ReflectionTestUtils.setField(sameId, "id", 1L);
        ReflectionTestUtils.setField(otherId, "id", 2L);

        assertThat(tag).isEqualTo(sameId);
        assertThat(tag).isNotEqualTo(otherId);
        assertThat(tag).hasSameHashCodeAs(sameId);
    }
}
