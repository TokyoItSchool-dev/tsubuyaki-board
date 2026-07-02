package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
import java.util.Optional;

@Controller
public class PostController {

    private static final String SESSION_AUTHOR = "postAuthor";
    private static final String SESSION_AVATAR_COLOR = "avatarColor";

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping({ "/", "/posts" })
    public String list(Model model, HttpServletRequest request,
            @RequestParam(required = false) String clientHash,
            @RequestParam(name = "q", required = false) String query) {
        String searchQuery = Optional.ofNullable(query).orElse("");
        List<Post> posts = postService.search(searchQuery);
        model.addAttribute("clientHash", resolveClientHash(clientHash, request));
        model.addAttribute("posts", posts);
        model.addAttribute("query", searchQuery);
        model.addAttribute("resultCount", posts.size());
        model.addAttribute("noSearchResults", !searchQuery.isBlank() && posts.isEmpty());
        model.addAttribute("showEmptyMessage", searchQuery.isBlank() && posts.isEmpty());
        return "posts/list";
    }

    @GetMapping("/posts/new")
    public String newForm(Model model, HttpSession session) {
        PostForm postForm = new PostForm();
        postForm.setAuthor((String) session.getAttribute(SESSION_AUTHOR));
        postForm.setAvatarColor((String) session.getAttribute(SESSION_AVATAR_COLOR));
        model.addAttribute("postForm", postForm);
        return "posts/form";
    }

    @PostMapping("/posts")
    public String create(@Valid @ModelAttribute("postForm") PostForm postForm, BindingResult bindingResult,
            HttpSession session) {
        if (bindingResult.hasErrors()) {
            return "posts/form";
        }
        postService.create(postForm.getAuthor(), postForm.getBody(), postForm.getAvatarColor());
        session.setAttribute(SESSION_AUTHOR, postForm.getAuthor());
        if (postForm.getAvatarColor() == null || postForm.getAvatarColor().isBlank()) {
            session.removeAttribute(SESSION_AVATAR_COLOR);
        } else {
            session.setAttribute(SESSION_AVATAR_COLOR, postForm.getAvatarColor());
        }
        return "redirect:/posts";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model, HttpServletRequest request,
            @RequestParam(required = false) String clientHash) {
        model.addAttribute("post", postService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
        String resolvedClientHash = resolveClientHash(clientHash, request);
        model.addAttribute("clientHash", resolvedClientHash);
        model.addAttribute("likeCount", postService.countLikes(id));
        model.addAttribute("liked", postService.isLiked(id, resolvedClientHash));
        return "posts/detail";
    }

    @PostMapping("/posts/{id}/likes")
    public String toggleLike(@PathVariable Long id, HttpServletRequest request,
            @RequestParam(required = false) String clientHash) {
        postService.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        String resolvedClientHash = resolveClientHash(clientHash, request);
        postService.toggleLike(id, resolvedClientHash);
        return "redirect:/posts/" + id + "?clientHash=" + resolvedClientHash;
    }

    private String resolveClientHash(String clientHash, HttpServletRequest request) {
        if (clientHash != null && !clientHash.isBlank()) {
            return clientHash;
        }
        return clientHash(request);
    }

    private String clientHash(HttpServletRequest request) {
        String userAgent = Optional.ofNullable(request.getHeader("User-Agent")).orElse("");
        String source = request.getRemoteAddr() + "|" + userAgent;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(source.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available.", e);
        }
    }
}
