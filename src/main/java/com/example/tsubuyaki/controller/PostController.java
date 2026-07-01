package com.example.tsubuyaki.controller;

import java.time.Instant;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;

@Controller
public class PostController {

    // Controller は画面からのリクエストを受け取り、業務処理を Service に委譲する。
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // 投稿一覧画面を表示する。Service から取得した最新投稿を posts としてビューへ渡す。
    @GetMapping({ "/", "/posts", "/posts/" })
    public String list(Model model) {
        model.addAttribute("posts", postService.latest());
        return "posts/list";
    }

    // 新規投稿画面を表示する。フォーム入力値を受け取るための postForm を model に積む。
    @GetMapping("/posts/new")
    public String newForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        return "posts/form";
    }

    // 投稿ボタン押下時の処理。入力値を検証し、問題がなければ投稿を保存して一覧へ戻す。
    @PostMapping({ "/posts", "/posts/new" })
    public String create(@Valid @ModelAttribute("postForm") PostForm postForm, BindingResult bindingResult) {
        // 入力エラーがある場合は INSERT せず、同じフォーム画面にエラーを表示する。
        if (bindingResult.hasErrors()) {
            return "posts/form";
        }

        // フォームDTOをPostエンティティへ変換し、Service経由でRepository.saveを実行する。
        postService.create(postForm.toPost(Instant.now()));
        // POST後の再送信を避けるため、一覧画面へリダイレクトする。
        return "redirect:/posts";
    }

    // 演習中に追加するエンドポイント:
    //   @GetMapping("/posts/{id}")       // 詳細
}
