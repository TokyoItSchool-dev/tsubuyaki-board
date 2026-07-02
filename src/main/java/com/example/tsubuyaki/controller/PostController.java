package com.example.tsubuyaki.controller;

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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

@Controller
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping({ "/", "/posts", "/posts/" })
    public String list(@RequestParam(name = "q", required = false) String query, Model model) {
        String keyword = normalizeQuery(query);
        if (hasText(keyword)) {
            model.addAttribute("posts", postService.searchByBodyContaining(keyword));
            model.addAttribute("q", keyword);
            return "posts/list";
        }
        model.addAttribute("posts", postService.findLatest50());
        model.addAttribute("q", "");
        return "posts/list";
    }

    @GetMapping("/tags/{name}")
    public String listByTag(@PathVariable String name, Model model) {
        String tagName = normalizeQuery(name).toLowerCase(Locale.ROOT);
        model.addAttribute("posts", postService.findByTagName(tagName));
        model.addAttribute("q", "");
        model.addAttribute("tagName", tagName);
        return "posts/list";
    }

    @GetMapping("/posts/new")
    public String newForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        return "posts/form";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model) {
        return postService.findById(id)
                .map(post -> {
                    model.addAttribute("post", post);
                    model.addAttribute("postId", id);
                    model.addAttribute("likeCount", postService.countLikes(id));
                    model.addAttribute("comments", postService.findCommentsByPostId(id));
                    return "posts/detail";
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/posts/{id}/comments")
    public String createComment(@PathVariable Long id, @RequestParam String body) {
        postService.createComment(id, body);
        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts/{id}/likes")
    public String toggleLike(@PathVariable Long id, HttpServletRequest request) {
        postService.toggleLike(id, clientHash(request));
        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts/{id}/delete")
    public String delete(@PathVariable Long id) {
        postService.delete(id);
        return "redirect:/posts";
    }

    @PostMapping("/posts")
    public String create(
            @Valid @ModelAttribute PostForm form,
            BindingResult result,
            @RequestParam(name = "tagNames", required = false) List<String> tagNames) {
        if (result.hasErrors()) {
            return "posts/form";
        }
        postService.create(form.getAuthor(), form.getBody(), form.getAvatarColor(), toTagNamesText(tagNames));
        return "redirect:/posts";
    }

    private static String clientHash(HttpServletRequest request) {
        String remoteAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        return sha256Prefix(nullToEmpty(remoteAddress) + nullToEmpty(userAgent));
    }

    private static String sha256Prefix(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String normalizeQuery(String query) {
        return query == null ? "" : query.trim();
    }

    private static String toTagNamesText(List<String> tagNames) {
        if (tagNames == null) {
            return "";
        }
        return String.join(" ", tagNames);
    }
}
