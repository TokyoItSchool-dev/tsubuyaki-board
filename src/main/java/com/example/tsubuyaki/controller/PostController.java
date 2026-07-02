package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.ReplyThreadItem;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import com.example.tsubuyaki.web.dto.ReplyForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
import java.util.List;
import java.util.NoSuchElementException;

@Controller
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping({ "/", "/posts", "/posts/" })
    public String list(@RequestParam(name = "q", required = false) String query, Model model) {
        model.addAttribute("posts", postsFor(query));
        model.addAttribute("q", nullToEmpty(query).trim());
        return "posts/list";
    }

    @GetMapping("/posts/new")
    public String newForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        return "posts/form";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model, HttpServletRequest request) {
        String clientHash = clientHash(request);
        addDetailAttributes(id, model, clientHash, new ReplyForm());
        return "posts/detail";
    }

    @PostMapping("/posts/{id}/replies")
    public String createReply(
            @PathVariable Long id,
            @Valid ReplyForm replyForm,
            BindingResult bindingResult,
            Model model,
            HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            addDetailAttributes(id, model, clientHash(request), replyForm);
            return "posts/detail";
        }
        try {
            postService.createReply(id, replyForm.getParentReplyId(), replyForm.getAuthor(), replyForm.getBody());
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "投稿または返信が見つかりません", e);
        }
        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts/{postId}/replies/{replyId}/read")
    public String toggleReplyRead(@PathVariable Long postId, @PathVariable Long replyId) {
        try {
            postService.toggleReplyRead(postId, replyId);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "返信が見つかりません", e);
        }
        return "redirect:/posts/" + postId;
    }

    private void addDetailAttributes(Long id, Model model, String clientHash, ReplyForm replyForm) {
        Post post = postService.findDetailPost(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("post", post);
        model.addAttribute("likeCount", postService.countLikes(id));
        model.addAttribute("liked", postService.hasLiked(id, clientHash));
        model.addAttribute("replies", replyThread(id));
        model.addAttribute("replyForm", replyForm);
    }

    @PostMapping("/posts/{id}/likes")
    public String toggleLike(@PathVariable Long id, HttpServletRequest request) {
        try {
            postService.toggleLike(id, clientHash(request));
        } catch (java.util.NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "投稿が見つかりません", e);
        }
        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts/{id}/delete")
    public String delete(@PathVariable Long id) {
        try {
            postService.deletePost(id);
        } catch (java.util.NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "投稿が見つかりません", e);
        }
        return "redirect:/posts";
    }

    @PostMapping("/posts")
    public String create(@Valid PostForm postForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "posts/form";
        }
        postService.createPost(postForm.getAuthor(), postForm.getAvatarColor(), postForm.getBody());
        return "redirect:/posts";
    }

    private List<Post> latestPosts() {
        List<Post> posts = postService.findLatest50Posts();
        if (posts == null) {
            return List.of();
        }
        return posts;
    }

    private List<Post> postsFor(String query) {
        String normalizedQuery = nullToEmpty(query).trim();
        if (normalizedQuery.isEmpty()) {
            return latestPosts();
        }

        List<Post> posts = postService.searchPosts(normalizedQuery);
        if (posts == null) {
            return List.of();
        }
        return posts;
    }

    private List<ReplyThreadItem> replyThread(Long postId) {
        List<ReplyThreadItem> replies = postService.findReplyThread(postId);
        if (replies == null) {
            return List.of();
        }
        return replies;
    }

    private static String clientHash(HttpServletRequest request) {
        String remoteAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String source = nullToEmpty(remoteAddress) + nullToEmpty(userAgent);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(source.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 が利用できません", e);
        }
    }

    private static String nullToEmpty(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }
}
