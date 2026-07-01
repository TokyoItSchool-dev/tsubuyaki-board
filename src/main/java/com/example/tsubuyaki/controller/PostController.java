package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
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

@Controller
public class PostController {

    private static final List<String> AUTHOR_ICON_COLORS = List.of(
            "#2563EB", "#0891B2", "#16A34A", "#F97316", "#DB2777");

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping({ "/", "/posts", "/posts/" })
    public String list(
            @RequestParam(name = "q", required = false) String query,
            Model model,
            HttpServletRequest request) {
        String normalizedQuery = query == null ? "" : query.trim();
        boolean searchExecuted = !normalizedQuery.isEmpty();
        List<Post> posts = searchExecuted
                ? postService.searchWithLikes(normalizedQuery, clientHash(request))
                : postService.latestWithLikes(clientHash(request));
        long searchResultCount = searchExecuted ? postService.countSearchResults(normalizedQuery) : posts.size();

        model.addAttribute("posts", posts);
        model.addAttribute("query", normalizedQuery);
        model.addAttribute("searchExecuted", searchExecuted);
        model.addAttribute("searchResultCount", searchResultCount);
        return "posts/list";
    }

    @GetMapping("/posts/new")
    public String newForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        addAuthorIconColors(model);
        return "posts/form";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model, HttpServletRequest request) {
        model.addAttribute("post", postService.findByIdWithLike(id, clientHash(request))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
        return "posts/detail";
    }

    @PostMapping("/posts")
    public String create(@Valid PostForm postForm, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            addAuthorIconColors(model);
            return "posts/form";
        }
        postService.create(postForm.getAuthor(), postForm.getBody(), postForm.getAuthorIconColor());
        return "redirect:/posts";
    }

    @PostMapping("/posts/{id}/likes")
    public String toggleLike(
            @PathVariable Long id,
            @RequestParam(defaultValue = "/posts") String returnTo,
            HttpServletRequest request) {
        postService.toggleLike(id, clientHash(request))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return "redirect:" + redirectPath(id, returnTo);
    }

    private static String redirectPath(Long postId, String returnTo) {
        String detailPath = "/posts/" + postId;
        if (detailPath.equals(returnTo)) {
            return detailPath;
        }
        return "/posts";
    }

    private static void addAuthorIconColors(Model model) {
        model.addAttribute("authorIconColors", AUTHOR_ICON_COLORS);
    }

    private static String clientHash(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String source = request.getRemoteAddr() + (userAgent == null ? "" : userAgent);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(source.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", e);
        }
    }
}
