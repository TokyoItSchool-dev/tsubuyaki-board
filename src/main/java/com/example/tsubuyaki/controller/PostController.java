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

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
public class PostController {

    private static final String RETURN_TO_LIST = "list";
    private static final String RETURN_TO_DETAIL = "detail";

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping({ "/", "/posts" })
    public String list(@RequestParam(name = "q", required = false) String query, Model model) {
        String normalizedQuery = normalizeQuery(query);
        boolean searched = !normalizedQuery.isEmpty();

        model.addAttribute("posts", searched ? postService.searchByBody(normalizedQuery) : postService.latest());
        model.addAttribute("query", normalizedQuery);
        model.addAttribute("searched", searched);
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
        postService.create(postForm.getAuthor(), postForm.getBody(), postForm.getBackgroundColor());
        return "redirect:/posts";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("post", postService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND)));
        return "posts/detail";
    }

    @PostMapping("/posts/{id}/likes")
    public String toggleLike(
            @PathVariable Long id,
            @RequestParam(defaultValue = RETURN_TO_DETAIL) String returnTo,
            HttpServletRequest request) {
        if (!postService.toggleLike(id, clientHash(request))) {
            throw new ResponseStatusException(NOT_FOUND);
        }
        return likeRedirectUrl(id, returnTo);
    }

    private String likeRedirectUrl(Long id, String returnTo) {
        if (RETURN_TO_LIST.equals(returnTo)) {
            return "redirect:/posts";
        }
        return "redirect:/posts/" + id;
    }

    private String normalizeQuery(String query) {
        if (query == null) {
            return "";
        }
        return query.trim();
    }

    private String clientHash(HttpServletRequest request) {
        String source = request.getRemoteAddr() + userAgent(request);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(source.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash, 0, 4);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private String userAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return "";
        }
        return userAgent;
    }
}
