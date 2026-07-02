package com.example.tsubuyaki.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @ModelAttribute("avatarColors")
    public List<String> avatarColors() {
        return List.of("red", "blue", "green", "yellow", "gray");
    }

    @GetMapping({ "/", "/posts", "/posts/" })
    public String list(@RequestParam(name = "q", required = false) String q, Model model) {
        model.addAttribute("posts", postService.search(q));
        model.addAttribute("q", q == null ? "" : q);
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
    public String detail(@PathVariable Long id, Model model) {
        return postService.findById(id)
                .map(post -> {
                    model.addAttribute("post", post);
                    model.addAttribute("postId", id);
                    model.addAttribute("likeCount", postService.countLikes(id));
                    return "posts/detail";
                })
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }

    @GetMapping("/tags/{name}")
    public String tagPosts(@PathVariable String name, Model model) {
        String normalized = name.toLowerCase(Locale.ROOT);
        model.addAttribute("tagName", normalized);
        model.addAttribute("posts", postService.findByTag(normalized));
        return "tags/show";
    }

    @PostMapping("/posts/{id}/likes")
    public String toggleLike(@PathVariable Long id, HttpServletRequest request) {
        if (postService.findById(id).isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND);
        }

        postService.toggleLike(id, clientHash(request));
        return "redirect:/posts/" + id;
    }

    private static String clientHash(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String source = request.getRemoteAddr() + (userAgent == null ? "" : userAgent);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(source.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
