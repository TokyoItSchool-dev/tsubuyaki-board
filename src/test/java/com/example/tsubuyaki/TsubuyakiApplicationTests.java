package com.example.tsubuyaki;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2")
class TsubuyakiApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PostRepository postRepository;

    @Test
    void contextLoads() {
        // Spring コンテキストが h2 プロファイルで起動できることだけを確認する。
    }

    @Test
    void health_ヘルスチェック_UPを返す() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/actuator/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }

    @Test
    void 投稿削除_POST後_一覧に表示されない() {
        Post post = postRepository.save(new Post(
                "delete-user",
                "delete-target-body",
                Instant.parse("2026-05-23T10:00:00Z")));

        ResponseEntity<String> deleteResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/posts/" + post.getId() + "/delete", null, String.class);
        ResponseEntity<String> listResponse = restTemplate.getForEntity(
                "http://localhost:" + port + "/posts", String.class);

        assertThat(deleteResponse.getStatusCode().is2xxSuccessful()
                || deleteResponse.getStatusCode().is3xxRedirection()).isTrue();
        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).doesNotContain("delete-target-body");
    }
}
