package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

@Controller
public class PostController {

    private static final int CLIENT_HASH_LENGTH = 8;

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping({ "/", "/posts", "/posts/" })
    public String list(@RequestParam(name = "q", required = false) String q, Model model) {
        String keyword = postService.normalizeKeyword(q);
        model.addAttribute("posts", postService.searchByBody(keyword));
        model.addAttribute("q", keyword);
        return "posts/list";
    }

    @GetMapping("/posts/form")
    public String newForm(@RequestParam(name = "q", required = false) String q, Model model) {
        model.addAttribute("postForm", new PostForm());
        model.addAttribute("q", postService.normalizeKeyword(q));
        return "posts/form";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("post", postService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
        model.addAttribute("likeCount", postService.countLikes(id));
        return "posts/detail";
    }

    @PostMapping("/posts")
    public String create(@Valid PostForm postForm, BindingResult bindingResult,
            @RequestParam(name = "q", required = false) String q, Model model) {
        String keyword = postService.normalizeKeyword(q);
        if (bindingResult.hasErrors()) {
            model.addAttribute("q", keyword);
            return "posts/form";
        }
        postService.create(postForm.getAuthor(), postForm.getBody());
        if (StringUtils.hasText(keyword)) {
            return "redirect:/posts?q=" + UriUtils.encodeQueryParam(keyword, StandardCharsets.UTF_8);
        }
        return "redirect:/posts";
    }

    @PostMapping("/posts/{id}/likes")
    public String toggleLike(@PathVariable Long id, HttpServletRequest request) {
        postService.toggleLike(id, clientHash(request))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return "redirect:/posts/" + id;
    }

    private String clientHash(HttpServletRequest request) {
        String source = Objects.toString(request.getRemoteAddr(), "")
                + Objects.toString(request.getHeader("User-Agent"), "");
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(source.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, CLIENT_HASH_LENGTH);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
