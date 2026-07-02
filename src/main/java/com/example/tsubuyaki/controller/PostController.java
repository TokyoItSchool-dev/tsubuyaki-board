package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.service.ClientHashGenerator;
import com.example.tsubuyaki.service.PostLikeService;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class PostController {

    private final PostService postService;

    private final PostLikeService postLikeService;

    private final ClientHashGenerator clientHashGenerator;

    public PostController(PostService postService, PostLikeService postLikeService,
            ClientHashGenerator clientHashGenerator) {
        this.postService = postService;
        this.postLikeService = postLikeService;
        this.clientHashGenerator = clientHashGenerator;
    }

    @GetMapping({ "/", "/posts" })
    public String list(Model model) {
        model.addAttribute("posts", postService.latest());
        return "posts/list";
    }

    @GetMapping("/posts/new")
    public String newForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        return "posts/form";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model) {
        // パス変数のidに対応する投稿を取得し、詳細テンプレートで参照できるようpost属性へ格納する。
        model.addAttribute("post", postService.findById(id));
        // 詳細画面で現在のいいね総数を表示できるよう、投稿idに紐づく件数をModelへ格納する。
        model.addAttribute("likeCount", postLikeService.countByPostId(id));
        return "posts/detail";
    }

    @PostMapping("/posts/{id}/likes")
    public String toggleLike(@PathVariable Long id, HttpServletRequest request) {
        // 接続元IPアドレスとUser-AgentからclientHashを作り、同一クライアントのトグル判定に使う。
        String clientHash = clientHashGenerator.generate(request.getRemoteAddr(), request.getHeader("User-Agent"));
        // いいねが未登録なら保存し、登録済みなら解除するトグル処理をServiceへ委譲する。
        postLikeService.toggle(id, clientHash);
        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts")
    public String create(@Valid @ModelAttribute("postForm") PostForm postForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "posts/form";
        }

        postService.create(postForm);
        return "redirect:/posts";
    }

}
