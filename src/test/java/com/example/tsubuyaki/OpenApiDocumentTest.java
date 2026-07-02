package com.example.tsubuyaki;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiDocumentTest {

    @Test
    @DisplayName("OpenAPIドキュメント_api_postsの構造を定義する")
    void openApiDocument_definesApiPostsSchema() throws IOException {
        ClassPathResource resource = new ClassPathResource("static/openapi.yaml");

        Map<String, Object> document;
        try (InputStream inputStream = resource.getInputStream()) {
            document = new Yaml().load(inputStream);
        }

        Map<String, Object> paths = map(document.get("paths"));
        Map<String, Object> apiPosts = map(paths.get("/api/posts"));
        Map<String, Object> get = map(apiPosts.get("get"));
        Map<String, Object> responses = map(get.get("responses"));
        Map<String, Object> ok = map(responses.get("200"));
        Map<String, Object> content = map(ok.get("content"));
        Map<String, Object> json = map(content.get("application/json"));
        Map<String, Object> schema = map(json.get("schema"));
        Map<String, Object> items = map(schema.get("items"));
        Map<String, Object> components = map(document.get("components"));
        Map<String, Object> schemas = map(components.get("schemas"));
        Map<String, Object> post = map(schemas.get("Post"));
        Map<String, Object> properties = map(post.get("properties"));

        assertThat(get.get("summary")).isEqualTo("投稿一覧 API");
        assertThat(ok.get("description")).isEqualTo("投稿一覧の JSON を返す");
        assertThat(schema.get("type")).isEqualTo("array");
        assertThat(items.get("$ref")).isEqualTo("#/components/schemas/Post");
        assertThat(post.get("type")).isEqualTo("object");
        assertThat(properties).containsKeys(
                "id", "author", "body", "avatarColor", "createdAt", "updatedAt", "tags", "likesCount");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return (Map<String, Object>) value;
    }
}
