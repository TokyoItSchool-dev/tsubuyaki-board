package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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

@Controller
public class PostController {

    private static final int AUTHOR_MAX_LENGTH = 30;

    private static final int BODY_MAX_LENGTH = 280;

    private static final int PAGE_SIZE = 10;

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping({ "/", "/posts", "/posts/" })
    public String list(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "page", defaultValue = "1") int page,
            Model model) {
        String normalizedQuery = query == null ? "" : query;
        int currentPage = normalizePage(page);
        model.addAttribute("query", normalizedQuery);
        if (normalizedQuery.isBlank()) {
            addPageAttributes(model, postService.latestPage(currentPage - 1, PAGE_SIZE), currentPage);
        } else {
            addPageAttributes(model, postService.searchByBodyPage(normalizedQuery, currentPage - 1, PAGE_SIZE), currentPage);
        }
        return "posts/list";
    }

    @GetMapping("/posts/new")
    public String newForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        addFormOptions(model);
        return "posts/form";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute(
                "post",
                postService.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
        model.addAttribute("likeCount", postService.countLikes(id));
        return "posts/detail";
    }

    @GetMapping("/tags/{name}")
    public String taggedPosts(
            @PathVariable String name,
            @RequestParam(name = "page", defaultValue = "1") int page,
            Model model) {
        int currentPage = normalizePage(page);
        model.addAttribute("query", "");
        model.addAttribute("tagName", name);
        addPageAttributes(model, postService.findByTagPage(name, currentPage - 1, PAGE_SIZE), currentPage);
        return "posts/list";
    }

    @PostMapping("/posts/{id}/likes")
    public String toggleLike(@PathVariable Long id, HttpServletRequest request) {
        if (!postService.toggleLike(id, clientHash(request))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts")
    public String create(@Valid PostForm postForm, BindingResult bindingResult, Model model) {
        rejectTrimmedLengthErrors(postForm, bindingResult);
        rejectInvalidAvatarColor(postForm, bindingResult);
        if (bindingResult.hasErrors()) {
            addFormOptions(model);
            return "posts/form";
        }
        postService.create(postForm.trimmedAuthor(), postForm.trimmedBody(), postForm.trimmedAvatarColor());
        return "redirect:/posts";
    }

    private void addFormOptions(Model model) {
        model.addAttribute("avatarColors", PostForm.avatarColorOptions());
    }

    private int normalizePage(int page) {
        return Math.max(page, 1);
    }

    private void addPageAttributes(Model model, Page<Post> postPage, int currentPage) {
        model.addAttribute("posts", postPage.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", postPage.getTotalPages());
        model.addAttribute("hasPreviousPage", postPage.hasPrevious());
        model.addAttribute("hasNextPage", postPage.hasNext());
        model.addAttribute("previousPage", Math.max(currentPage - 1, 1));
        model.addAttribute("nextPage", currentPage + 1);
    }

    private void rejectTrimmedLengthErrors(PostForm postForm, BindingResult bindingResult) {
        rejectIfTooLong(
                "author",
                postForm.trimmedAuthor(),
                AUTHOR_MAX_LENGTH,
                "投稿者は30文字以内で入力してください",
                bindingResult);
        rejectIfTooLong(
                "body",
                postForm.trimmedBody(),
                BODY_MAX_LENGTH,
                "本文は280文字以内で入力してください",
                bindingResult);
    }

    private void rejectInvalidAvatarColor(PostForm postForm, BindingResult bindingResult) {
        if (postForm.trimmedAvatarColor() != null
                && !postForm.trimmedAvatarColor().isBlank()
                && !postForm.hasAllowedAvatarColor()) {
            bindingResult.rejectValue("avatarColor", "AllowedValues", "アバター色を選択肢から選んでください");
        }
    }

    private void rejectIfTooLong(
            String field,
            String value,
            int maxLength,
            String message,
            BindingResult bindingResult) {
        if (value != null && value.length() > maxLength) {
            bindingResult.rejectValue(field, "Size", message);
        }
    }

    private String clientHash(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String source = request.getRemoteAddr() + (userAgent == null ? "" : userAgent);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(source.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", e);
        }
    }

    // 演習中に追加するエンドポイント:
}
