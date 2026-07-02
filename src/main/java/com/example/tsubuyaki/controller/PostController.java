package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.service.ClientHashService;
import com.example.tsubuyaki.service.PostDetail;
import com.example.tsubuyaki.service.PostRegistrationException;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PostController {

    private final PostService postService;
    private final ClientHashService clientHashService;

    public PostController(PostService postService, ClientHashService clientHashService) {
        this.postService = postService;
        this.clientHashService = clientHashService;
    }

    @GetMapping({ "/", "/posts" })
    public String list(@RequestParam(name = "q", required = false) String keyword, Model model) {
        model.addAttribute("posts", postService.search(keyword));
        model.addAttribute("q", keyword);
        return "posts/list";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model, HttpServletRequest request,
            HttpServletResponse response) {
        String clientHash = clientHashService.from(request);
        return postService.findDetail(id, clientHash)
                .map(detail -> detailView(id, detail, model))
                .orElseGet(() -> notFound(response));
    }

    @GetMapping("/posts/new")
    public String newForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        return "posts/form";
    }

    @PostMapping("/posts")
    public String create(@Valid @ModelAttribute("postForm") PostForm postForm,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "posts/form";
        }

        try {
            postService.create(postForm.getAuthor(), postForm.getBody());
        } catch (PostRegistrationException e) {
            bindingResult.reject("post.create.failed", e.getMessage());
            return "posts/form";
        }

        return "redirect:/posts";
    }

    @PostMapping("/posts/{id}/likes")
    public String like(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
        String clientHash = clientHashService.from(request);
        return postService.toggleLike(id, clientHash)
                .map(result -> "redirect:/posts/" + id)
                .orElseGet(() -> notFound(response));
    }

    private String detailView(Long id, PostDetail detail, Model model) {
        model.addAttribute("post", detail.post());
        model.addAttribute("postId", id);
        model.addAttribute("likeCount", detail.likeCount());
        model.addAttribute("liked", detail.liked());
        return "posts/detail";
    }

    private String notFound(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return "error/404";
    }
}
