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
import java.util.List;
import java.util.HexFormat;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @ModelAttribute("avatarColors")
    public List<String> avatarColors() {
        return List.of("blue", "green", "red", "pink", "yellow", "purple");
    }

    @GetMapping({ "/", "/posts" })
    public String list(@RequestParam(name = "q", required = false) String q, Model model) {
        String keyword = nullToEmpty(q).trim();
        model.addAttribute("posts", keyword.isEmpty() ? postService.latest() : postService.search(keyword));
        model.addAttribute("q", keyword);
        return "posts/list";
    }

    @GetMapping("/tags/{name}")
    public String tag(@PathVariable String name, Model model) {
        String tagName = nullToEmpty(name).trim();
        model.addAttribute("posts", postService.findByTagName(tagName));
        model.addAttribute("q", "");
        model.addAttribute("tagName", tagName);
        return "posts/list";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("post", postService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND)));
        model.addAttribute("likeCount", postService.countLikes(id));
        return "posts/detail";
    }

    @GetMapping("/posts/new")
    public String newForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        return "posts/form";
    }

    @PostMapping("/posts")
    public String create(
            @Valid @ModelAttribute("postForm") PostForm postForm,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "posts/form";
        }

        postService.create(postForm.getAuthor(), postForm.getBody(), postForm.getAvatarColor());
        return "redirect:/posts";
    }

    @PostMapping("/posts/{id}/likes")
    public String toggleLike(@PathVariable Long id, HttpServletRequest request) {
        String clientHash = clientHash(request.getRemoteAddr(), request.getHeader("User-Agent"));
        postService.toggleLike(id, clientHash)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts/{id}/delete")
    public String delete(@PathVariable Long id) {
        if (!postService.delete(id)) {
            throw new ResponseStatusException(NOT_FOUND);
        }
        return "redirect:/posts";
    }

    private static String clientHash(String ipAddress, String userAgent) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((ipAddress + nullToEmpty(userAgent)).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
