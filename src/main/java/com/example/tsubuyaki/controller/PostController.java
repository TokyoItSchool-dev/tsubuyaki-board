package com.example.tsubuyaki.controller;

import java.time.LocalDateTime;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.ClientHashService;
import com.example.tsubuyaki.service.PostLikeService;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;

@Controller
public class PostController {

    private static final String USER_AGENT_HEADER = "User-Agent";

    // Controller は画面からのリクエストを受け取り、業務処理を Service に委譲する。
    private final PostService postService;
    private final PostLikeService postLikeService;
    private final ClientHashService clientHashService;

    public PostController(PostService postService, PostLikeService postLikeService,
            ClientHashService clientHashService) {
        this.postService = postService;
        this.postLikeService = postLikeService;
        this.clientHashService = clientHashService;
    }

    // 投稿一覧画面を表示する。q があれば本文検索し、結果を posts としてビューへ渡す。
    @GetMapping({ "/", "/posts", "/posts/" })
    public String list(@RequestParam(name = "q", required = false) String q, Model model) {
        model.addAttribute("posts", postService.search(q));
        model.addAttribute("q", q);
        return "posts/list";
    }

    // 投稿詳細画面を表示する。URLのIDに一致する投稿を post としてビューへ渡す。
    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model) {
        // 指定IDの投稿が存在しない場合は 404 Not Found を返す。
        Post post = postService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("post", post);
        model.addAttribute("likeCount", postLikeService.countLikes(id));
        return "posts/detail";
    }

    // 新規投稿画面を表示する。フォーム入力値を受け取るための postForm を model に積む。
    @GetMapping("/posts/new")
    public String newForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        return "posts/form";
    }

    // 投稿ボタン押下時の処理。入力値を検証し、問題がなければ投稿を保存して一覧へ戻す。
    @PostMapping({ "/post", "/posts", "/posts/new" })
    public String create(@Valid @ModelAttribute("postForm") PostForm postForm, BindingResult bindingResult) {
        // 入力エラーがある場合は INSERT せず、同じフォーム画面にエラーを表示する。
        if (bindingResult.hasErrors()) {
            return "posts/form";
        }

        // フォームDTOをPostエンティティへ変換し、Service経由でRepository.saveを実行する。
        postService.create(postForm.toPost(LocalDateTime.now()));
        // POST後の再送信を避けるため、一覧画面へリダイレクトする。
        return "redirect:/posts";
    }

    // Likeボタン押下時の処理。クライアントを識別し、いいねの追加・解除をトグルする。
    @PostMapping("/posts/{id}/likes")
    public String toggleLike(@PathVariable Long id, HttpServletRequest request) {
        String clientHash = clientHashService.hash(
                request.getRemoteAddr(), request.getHeader(USER_AGENT_HEADER));
        postLikeService.toggleLike(id, clientHash);
        return "redirect:/posts/" + id;
    }
}
