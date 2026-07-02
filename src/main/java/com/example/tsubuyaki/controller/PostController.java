package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class PostController {

    private static final String POSTS_ATTRIBUTE = "posts";
    private static final String POSTS_LIST_VIEW = "posts/list.html";
    private static final String POST_ATTRIBUTE = "post";
    private static final String POST_FORM_ATTRIBUTE = "postForm";
    private static final String POST_FORM_VIEW = "posts/form.html";
    private static final String POST_DETAIL_VIEW = "posts/detail.html";

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping({ "/", "/posts" })
    public String list(Model model) {
        model.addAttribute(POSTS_ATTRIBUTE, postService.findLatest50());
        return POSTS_LIST_VIEW;
    }

    @GetMapping("/posts/new")
    public String newForm(Model model) {
        model.addAttribute(POST_FORM_ATTRIBUTE, new PostForm());
        return POST_FORM_VIEW;
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute(POST_ATTRIBUTE, postService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
        return POST_DETAIL_VIEW;
    }

    @PostMapping("/posts")
    public String create(@Valid PostForm postForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return POST_FORM_VIEW;
        }

        postService.create(postForm.getAuthor(), postForm.getBody());
        return "redirect:/posts";
    }
}
