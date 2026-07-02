package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.ClientHashService;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
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

import java.util.List;

@Controller
public class PostController {

    private final PostService postService;

    private final ClientHashService clientHashService;

    public PostController(PostService postService, ClientHashService clientHashService) {
        this.postService = postService;
        this.clientHashService = clientHashService;
    }

    @ModelAttribute("avatarColors")
    public List<String> avatarColors() {
        return PostForm.AVATAR_COLORS;
    }

    @GetMapping({ "/", "/posts" })
    public String list(@RequestParam(name = "q", required = false) String q, Model model) {
        model.addAttribute("posts", postService.search(q));
        model.addAttribute("q", q);
        return "posts/list";
    }

    @GetMapping("/posts/new")
    public String newForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        return "posts/form";
    }

    @PostMapping("/posts")
    public String create(@Valid @ModelAttribute("postForm") PostForm postForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "posts/form";
        }
        postService.create(postForm.getAuthor(), postForm.getBody(), postForm.getAvatarColor());
        return "redirect:/posts";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model, HttpServletRequest request) {
        Post post = postService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        String clientHash = clientHash(request);
        model.addAttribute("post", post);
        model.addAttribute("likeCount", postService.countLikes(id));
        model.addAttribute("likedByClient", postService.likedBy(id, clientHash));
        return "posts/detail";
    }

    @PostMapping("/posts/{id}/likes")
    public String toggleLike(@PathVariable Long id, HttpServletRequest request) {
        postService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        postService.toggleLike(id, clientHash(request));
        return "redirect:/posts/" + id;
    }

    private String clientHash(HttpServletRequest request) {
        return clientHashService.generate(request.getRemoteAddr(), request.getHeader("User-Agent"));
    }
}
