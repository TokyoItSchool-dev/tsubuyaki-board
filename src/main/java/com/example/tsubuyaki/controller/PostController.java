package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PostController {

    private static final List<AvatarColorOption> AVATAR_COLORS = List.of(
            new AvatarColorOption("red", "赤"),
            new AvatarColorOption("blue", "青"),
            new AvatarColorOption("green", "緑"),
            new AvatarColorOption("yellow", "黄"),
            new AvatarColorOption("purple", "紫")
    );

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping({ "/", "/posts", "/posts/" })
    public String list(@RequestParam(name = "q", required = false) String keyword, Model model) {
        if (StringUtils.hasText(keyword)) {
            String trimmedKeyword = keyword.trim();
            addPosts(model, postService.searchPosts(trimmedKeyword));
            model.addAttribute("keyword", trimmedKeyword);
            return "posts/list";
        }

        addPosts(model, postService.findLatestPosts());
        model.addAttribute("keyword", "");
        return "posts/list";
    }

    @GetMapping("/posts/new")
    public String newForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        addAvatarColors(model);
        return "posts/form";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model, HttpServletRequest request) {
        model.addAttribute("post", postService.findPost(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
        model.addAttribute("likeCount", postService.countLikes(id));
        model.addAttribute("liked", postService.isLiked(id, clientHash(request)));
        return "posts/detail";
    }

    @PostMapping("/posts/{id}/likes")
    public String toggleLike(@PathVariable Long id, HttpServletRequest request) {
        postService.toggleLike(id, clientHash(request));
        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts/{id}/delete")
    public String delete(@PathVariable Long id) {
        postService.deletePost(id);
        return "redirect:/posts";
    }

    @PostMapping("/posts")
    public String create(@Valid @ModelAttribute("postForm") PostForm postForm,
            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            addAvatarColors(model);
            return "posts/form";
        }

        postService.createPost(postForm.getAuthor(), postForm.getBody(), postForm.getAvatarColor());
        return "redirect:/posts";
    }

    private String clientHash(HttpServletRequest request) {
        String remoteAddress = request.getRemoteAddr() == null ? "" : request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent") == null ? "" : request.getHeader("User-Agent");
        byte[] digest = sha256(remoteAddress + userAgent);
        return HexFormat.of().formatHex(digest).substring(0, 8);
    }

    private byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }

    private void addAvatarColors(Model model) {
        model.addAttribute("avatarColors", AVATAR_COLORS);
    }

    private void addPosts(Model model, List<Post> posts) {
        Map<Long, Long> likeCounts = new LinkedHashMap<>();
        for (Post post : posts) {
            likeCounts.put(post.getId(), postService.countLikes(post.getId()));
        }
        model.addAttribute("posts", posts);
        model.addAttribute("likeCounts", likeCounts);
    }

    public record AvatarColorOption(String value, String label) {
    }
}
