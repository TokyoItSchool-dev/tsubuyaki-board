package com.example.tsubuyaki.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    @Test
    @DisplayName("取得_findByName_保存済みタグを名前で取得する")
    void 取得_findByName_保存済みタグを名前で取得する() {
        TagEntity saved = tagRepository.saveAndFlush(new TagEntity("SpringBoot"));

        Optional<TagEntity> actual = tagRepository.findByName("SpringBoot");

        assertThat(actual)
                .get()
                .satisfies(tag -> {
                    assertThat(tag.getId()).isEqualTo(saved.getId());
                    assertThat(tag.getName()).isEqualTo("SpringBoot");
                });
    }

    @Test
    @DisplayName("存在確認_existsByName_同じ名前のタグが存在することを判定する")
    void 存在確認_existsByName_同じ名前のタグが存在することを判定する() {
        tagRepository.saveAndFlush(new TagEntity("Java"));

        assertThat(tagRepository.existsByName("Java")).isTrue();
        assertThat(tagRepository.existsByName("Spring")).isFalse();
    }
}
