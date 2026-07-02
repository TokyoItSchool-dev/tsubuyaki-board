package com.example.tsubuyaki.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class TagTest {

    @Test
    @DisplayName("Tag_コンストラクタ_nameを保持する")
    void Tag_コンストラクタ_nameを保持する() {
        Tag tag = new Tag("研修");

        assertThat(tag.getId()).isNull();
        assertThat(tag.getName()).isEqualTo("研修");
    }

    @Test
    @DisplayName("Tag_JPA用コンストラクタ_空インスタンスを作成できる")
    void Tag_JPA用コンストラクタ_空インスタンスを作成できる() {
        Tag tag = new Tag();

        assertThat(tag.getId()).isNull();
        assertThat(tag.getName()).isNull();
    }

    @Test
    @DisplayName("Tag_equals_同じidなら等しい")
    void Tag_equals_同じidなら等しい() {
        Tag first = new Tag("java");
        Tag second = new Tag("spring");
        ReflectionTestUtils.setField(first, "id", 1L);
        ReflectionTestUtils.setField(second, "id", 1L);

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    @DisplayName("Tag_equals_idが異なるまたはTag以外なら等しくない")
    void Tag_equals_idが異なるまたはTag以外なら等しくない() {
        Tag first = new Tag("java");
        Tag second = new Tag("java");
        ReflectionTestUtils.setField(first, "id", 1L);
        ReflectionTestUtils.setField(second, "id", 2L);

        assertThat(first).isEqualTo(first);
        assertThat(first).isNotEqualTo(second);
        assertThat(first).isNotEqualTo("java");
    }
}
