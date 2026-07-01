package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostLikeService;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Controller
public class PostController {

    private final PostService postService;
    private final PostLikeService postLikeService;

    public PostController(PostService postService, PostLikeService postLikeService) {
        this.postService = postService;
        this.postLikeService = postLikeService;
    }

    @GetMapping({ "/", "/posts" })
    public String list(Model model) {
        List<Post> posts = postService.latest();
        model.addAttribute("posts", posts);
        model.addAttribute("likeCounts", likeCounts(posts));
        return "posts/list";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable long id, Model model) {
        try {
            model.addAttribute("post", postService.findById(id));
            model.addAttribute("likeCount", postLikeService.countByPostId(id));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "投稿が見つかりません。", e);
        }
        return "posts/detail";
    }

    @GetMapping("/posts/new")
    public String newForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        return "posts/form";
    }

    @PostMapping("/posts/{id}/likes")
    public String toggleLike(@PathVariable long id, HttpServletRequest request) {
        postLikeService.toggle(id, clientHash(request));
        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts")
    public String create(@Valid PostForm postForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "posts/form";
        }
        try {
            postService.create(postForm.getAuthor(), postForm.getBody());
        } catch (DataAccessException e) {
            bindingResult.reject("post.create.failed", "投稿の登録に失敗しました。時間をおいて再度お試しください。");
            return "posts/form";
        }
        return "redirect:/posts";
    }

    private Map<Long, Long> likeCounts(List<Post> posts) {
        Map<Long, Long> likeCounts = new LinkedHashMap<>();
        for (Post post : posts) {
            if (post.getId() != null) {
                likeCounts.put(post.getId(), postLikeService.countByPostId(post.getId()));
            }
        }
        return likeCounts;
    }

    private String clientHash(HttpServletRequest request) {
        String source = request.getRemoteAddr() + "|" + request.getHeader("User-Agent");
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(source.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available.", e);
        }
    }
}
