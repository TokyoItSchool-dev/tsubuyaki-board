package com.example.tsubuyaki.db;

import com.example.tsubuyaki.domain.Comment;
import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.Tag;
import jakarta.persistence.Column;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseCompatibilityTest {

    @Test
    @DisplayName("DB互換性_日時はTIMESTAMP6とLocalDateTimeで統一する")
    void databaseCompatibility_日時はTIMESTAMP6とLocalDateTimeで統一する() throws Exception {
        assertThat(Post.class.getDeclaredField("createdAt").getType()).isEqualTo(LocalDateTime.class);
        assertThat(Post.class.getDeclaredField("deletedAt").getType()).isEqualTo(LocalDateTime.class);
        assertThat(Tag.class.getDeclaredField("createdAt").getType()).isEqualTo(LocalDateTime.class);
        assertThat(Comment.class.getDeclaredField("createdAt").getType()).isEqualTo(LocalDateTime.class);
        assertThat(Post.class.getDeclaredField("createdAt").getAnnotation(Column.class).columnDefinition())
                .isEqualTo("TIMESTAMP(6)");
        assertThat(Post.class.getDeclaredField("deletedAt").getAnnotation(Column.class).columnDefinition())
                .isEqualTo("TIMESTAMP(6)");
        assertThat(Tag.class.getDeclaredField("createdAt").getAnnotation(Column.class).columnDefinition())
                .isEqualTo("TIMESTAMP(6)");
        assertThat(Comment.class.getDeclaredField("createdAt").getAnnotation(Column.class).columnDefinition())
                .isEqualTo("TIMESTAMP(6)");

        String migrations = String.join("\n",
                Files.readString(Path.of("src/main/resources/db/migration/V1__init.sql")),
                Files.readString(Path.of("src/main/resources/db/migration/V3__add_deleted_at_to_posts.sql")),
                Files.readString(Path.of("src/main/resources/db/migration/V4__tags.sql")),
                Files.readString(Path.of("src/main/resources/db/migration/V5__comments.sql")));

        assertThat(migrations).contains("created_at  TIMESTAMP(6)");
        assertThat(migrations).contains("deleted_at TIMESTAMP(6)");
        assertThat(migrations).doesNotContain("TIMESTAMP WITH TIME ZONE");
    }
}
