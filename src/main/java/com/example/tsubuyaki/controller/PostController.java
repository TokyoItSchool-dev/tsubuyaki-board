package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
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

@Controller
public class PostController {
    private static final String THEME_COLOR_ATTRIBUTE = "themeColor";
    private static final List<ThemeColorOption> THEME_COLORS = List.of(
            new ThemeColorOption("blue", "青", "#eef5ff"),
            new ThemeColorOption("green", "緑", "#effaf3"),
            new ThemeColorOption("pink", "ピンク", "#fff1f5"),
            new ThemeColorOption("gray", "グレー", "#f3f4f6"));

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @ModelAttribute
    public void addThemeColor(
            @RequestParam(name = THEME_COLOR_ATTRIBUTE, required = false) String requestedThemeColor,
            HttpSession session,
            Model model) {
        String themeColor = normalizeThemeColor((String) session.getAttribute(THEME_COLOR_ATTRIBUTE));
        if (requestedThemeColor != null) {
            themeColor = normalizeThemeColor(requestedThemeColor);
            if (themeColor.isBlank()) {
                session.removeAttribute(THEME_COLOR_ATTRIBUTE);
            } else {
                session.setAttribute(THEME_COLOR_ATTRIBUTE, themeColor);
            }
        }
        model.addAttribute(THEME_COLOR_ATTRIBUTE, themeColor);
        model.addAttribute("themeColorOptions", THEME_COLORS);
        model.addAttribute("themeBackgroundColor", themeBackgroundColor(themeColor));
    }

    @GetMapping({ "/", "/posts" })
    public String list(@RequestParam(name = "q", required = false) String q, Model model) {
        model.addAttribute("posts", q == null || q.isBlank() ? postService.latest() : postService.searchByBody(q));
        model.addAttribute("q", q == null ? "" : q);
        return "posts/list";
    }

    @GetMapping("/posts/new")
    public String newForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        return "posts/form";
    }

    @PostMapping("/posts")
    public String create(@Valid @ModelAttribute("postForm") PostForm form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "posts/form";
        }
        postService.create(form.getAuthor(), form.getBody(), form.getAvatarColor());
        return "redirect:/posts";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Post post = postService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("post", post);
        model.addAttribute("likeCount", postService.countLikes(id));
        return "posts/detail";
    }

    @PostMapping("/posts/{id}/likes")
    public String toggleLike(@PathVariable Long id, HttpServletRequest request) {
        if (postService.findById(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        postService.toggleLike(id, clientHash(request));
        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts/{id}/delete")
    public String delete(@PathVariable Long id) {
        if (!postService.delete(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return "redirect:/posts";
    }

    private String clientHash(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String source = request.getRemoteAddr() + "|" + (userAgent == null ? "" : userAgent);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(source.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }

    private String normalizeThemeColor(String themeColor) {
        if (themeColor == null || themeColor.isBlank()) {
            return "";
        }
        return THEME_COLORS.stream()
                .map(ThemeColorOption::value)
                .filter(themeColor::equals)
                .findFirst()
                .orElse("");
    }

    private String themeBackgroundColor(String themeColor) {
        return THEME_COLORS.stream()
                .filter(option -> option.value().equals(themeColor))
                .map(ThemeColorOption::backgroundColor)
                .findFirst()
                .orElse("");
    }

    public record ThemeColorOption(String value, String label, String backgroundColor) {
    }
}
