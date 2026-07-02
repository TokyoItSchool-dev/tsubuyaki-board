package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@Tag(name = "posts", description = "投稿API")
public class PostApiController {

    private final PostService postService;

    public PostApiController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    @Operation(summary = "投稿一覧を取得する")
    public List<PostResponse> list() {
        return postService.findLatest50().stream()
                .map(PostResponse::from)
                .toList();
    }

    public record PostResponse(
            Long id,
            String author,
            String body,
            String avatarColor,
            Instant createdAt) {

        private static PostResponse from(Post post) {
            return new PostResponse(
                    post.getId(),
                    post.getAuthor(),
                    post.getBody(),
                    post.getAvatarColor(),
                    post.getCreatedAt());
        }
    }
}
