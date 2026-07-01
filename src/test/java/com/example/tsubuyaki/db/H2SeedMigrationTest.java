package com.example.tsubuyaki.db;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class H2SeedMigrationTest {

    @Test
    @DisplayName("H2シーダー_起動時_投稿10件を投入し次IDが11になる")
    void H2シーダー_起動時_投稿10件を投入し次Idが11になる() throws SQLException {
        String url = "jdbc:h2:mem:seed_" + UUID.randomUUID().toString().replace("-", "")
                + ";MODE=Oracle;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;DATABASE_TO_UPPER=true";

        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/migration", "classpath:db/seed/h2")
                .load()
                .migrate();

        try (Connection connection = DriverManager.getConnection(url, "sa", "");
             Statement statement = connection.createStatement()) {
            assertThat(singleLong(statement, "SELECT COUNT(*) FROM posts")).isEqualTo(10);
            assertThat(singleLong(statement, "SELECT NEXT VALUE FOR posts_seq")).isEqualTo(11);
        }
    }

    private static long singleLong(Statement statement, String sql) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(sql)) {
            assertThat(resultSet.next()).isTrue();
            return resultSet.getLong(1);
        }
    }
}
