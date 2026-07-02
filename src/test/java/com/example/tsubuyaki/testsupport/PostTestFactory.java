package com.example.tsubuyaki.testsupport;

import com.example.tsubuyaki.domain.Post;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

public final class PostTestFactory {

    private static final LocalDateTime DEFAULT_CREATED_AT =
            LocalDateTime.of(2026, 5, 23, 10, 0);

    private PostTestFactory() {
    }

    public static Post post(String author, String body) {
        return new Post(author, body, DEFAULT_CREATED_AT);
    }

    public static Post postWithColor(String author, String body, String authorColor) {
        return new Post(author, body, authorColor, DEFAULT_CREATED_AT);
    }

    public static Post postWithId(Long id, String author, String body) {
        Post post = post(author, body);
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }

    public static Post postWithId(Long id, String author, String body,
            LocalDateTime createdAt) {
        Post post = new Post(author, body, createdAt);
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }

    public static Post postWithIdAndColor(Long id, String author, String body,
            String authorColor) {
        Post post = postWithColor(author, body, authorColor);
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }
}
