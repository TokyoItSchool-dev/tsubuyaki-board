package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PostApiController {

    private final PostService postService;

    /**
     * 投稿 API のコントローラを生成する。
     *
     * @param postService 投稿サービス
     */
    public PostApiController(PostService postService) {
        this.postService = postService;
    }

    /**
     * 最新の投稿一覧を JSON 形式で返す。
     *
     * @return 投稿レスポンスの一覧
     */
    @GetMapping("/api/posts")
    public List<PostResponse> posts() {
        // API では clientHash などの内部値を返さないよう DTO に詰め替える。
        return postService.latest().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 投稿エンティティを API 公開用のレスポンス DTO に変換する。
     *
     * @param post 投稿エンティティ
     * @return 投稿レスポンス
     */
    private PostResponse toResponse(Post post) {
        return new PostResponse(post.getId(), post.getAuthor(), post.getBody(), post.getCreatedAt(), post.getColor());
    }
}
