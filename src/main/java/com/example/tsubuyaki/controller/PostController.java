package com.example.tsubuyaki.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;

@Controller
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping({ "/", "/posts", "/posts/" })
    public String list(Model model) {
        model.addAttribute("posts", postService.latest());
        return "posts/list";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("postId", id);
        model.addAttribute("post", postService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "投稿が見つかりません")));
        return "posts/detail";
    }

    @GetMapping("/posts/new")
    public String newForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        setupCreateForm(model);
        return "posts/form";
    }

    @PostMapping("/posts")
    public String create(@Valid PostForm postForm, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            setupCreateForm(model);
            return "posts/form";
        }

        postService.create(postForm.getAuthor(), postForm.getBody());
        return "redirect:/posts";
    }

    @GetMapping("/posts/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        PostForm postForm = postService.findById(id)
                .map(post -> {
                    PostForm form = new PostForm();
                    form.setAuthor(post.getAuthor());
                    form.setBody(post.getBody());
                    return form;
                })
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "投稿が見つかりません"));

        model.addAttribute("postForm", postForm);
        setupEditForm(model, id);
        return "posts/form";
    }

    @PostMapping("/posts/{id}")
    public String update(@PathVariable Long id,
            @Valid PostForm postForm,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            setupEditForm(model, id);
            return "posts/form";
        }

        postService.update(id, postForm.getAuthor(), postForm.getBody())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "投稿が見つかりません"));
        return "redirect:/posts/" + id;
    }

    private void setupCreateForm(Model model) {
        model.addAttribute("formTitle", "新規投稿");
        model.addAttribute("formAction", "/posts");
        model.addAttribute("cancelUrl", "/posts");
    }

    private void setupEditForm(Model model, Long id) {
        model.addAttribute("formTitle", "投稿編集");
        model.addAttribute("formAction", "/posts/" + id);
        model.addAttribute("cancelUrl", "/posts/" + id);
    }
}
