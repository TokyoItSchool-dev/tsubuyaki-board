package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping({ "/", "/posts", "/posts/" })
    public String list(@RequestParam(name = "q", required = false) String q, Model model) {
        model.addAttribute("posts", postService.search(q));
        addSearchAttributes(model, q);
        return "posts/list";
    }

    @GetMapping("/posts/new")
    public String newForm(@RequestParam(name = "q", required = false) String q, Model model) {
        model.addAttribute("postForm", new PostForm());
        addSearchAttributes(model, q);
        return "posts/form";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id,
                         @RequestParam(name = "q", required = false) String q,
                         Model model) {
        model.addAttribute("post", postService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
        addSearchAttributes(model, q);
        return "posts/detail";
    }

    @PostMapping("/posts")
    public String create(@Valid @ModelAttribute("postForm") PostForm postForm,
                         BindingResult bindingResult,
                         @RequestParam(name = "q", required = false) String q,
                         Model model) {
        if (bindingResult.hasErrors()) {
            addSearchAttributes(model, q);
            return "posts/form";
        }

        postService.create(postForm.getAuthor(), postForm.getBody());
        return "redirect:/posts";
    }

    private void addSearchAttributes(Model model, String q) {
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("searchPerformed", StringUtils.hasText(q));
    }
}
