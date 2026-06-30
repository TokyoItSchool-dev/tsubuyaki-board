package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PostController {

    private static final int LATEST_POST_LIMIT = 50;

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping({ "/", "/posts" })
    public String list(Model model) {
        model.addAttribute("posts", postService.latest(LATEST_POST_LIMIT));
        return "posts/list";
    }

    @GetMapping("/posts/new")
    public String newForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        return "posts/form";
    }

    // 演習中に追加するエンドポイント:
    //   @PostMapping("/posts")           // 投稿登録
    //   @GetMapping("/posts/{id}")       // 詳細
}
