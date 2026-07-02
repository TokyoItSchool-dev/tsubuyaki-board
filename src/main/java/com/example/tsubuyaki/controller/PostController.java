package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.service.ClientHashService;
import com.example.tsubuyaki.service.PostLikeService;
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

import java.util.NoSuchElementException;

@Controller
public class PostController {

    private final PostService postService;
    private final PostLikeService postLikeService;
    private final ClientHashService clientHashService;

    public PostController(PostService postService, PostLikeService postLikeService,
            ClientHashService clientHashService) {
        this.postService = postService;
        this.postLikeService = postLikeService;
        this.clientHashService = clientHashService;
    }

    @GetMapping({ "/", "/posts", "/posts/" })
    public String list(@RequestParam(name = "q", required = false) String q, Model model) {
        String keyword = normalizeKeyword(q);
        model.addAttribute("posts", keyword.isEmpty() ? postService.latest() : postService.search(keyword));
        model.addAttribute("q", keyword);
        return "posts/list";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("post", postService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
        model.addAttribute("postId", id);
        model.addAttribute("likeCount", postLikeService.countLikes(id));
        return "posts/detail";
    }

    @GetMapping("/tags/{name}")
    public String tag(@PathVariable String name, Model model) {
        model.addAttribute("posts", postService.findByTag(name));
        model.addAttribute("q", "");
        model.addAttribute("tag", "");
        return "posts/list";
    }

    @GetMapping("/tags")
    public String tagSearch(@RequestParam(name = "tag", required = false) String tag, Model model) {
        String keyword = normalizeKeyword(tag);
        model.addAttribute("posts", keyword.isEmpty() ? postService.latest() : postService.searchByTag(keyword));
        model.addAttribute("q", "");
        model.addAttribute("tag", keyword);
        return "posts/list";
    }

    @GetMapping("/posts/new")
    public String newForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        return "posts/form";
    }

    @PostMapping("/posts")
    public String create(@Valid @ModelAttribute("postForm") PostForm postForm,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "posts/form";
        }

        postService.create(postForm.getAuthor(), postForm.getBody(), postForm.getAvatarColor());
        return "redirect:/posts";
    }

    @PostMapping("/posts/{id}/likes")
    public String toggleLike(@PathVariable Long id, HttpServletRequest request) {
        try {
            postLikeService.toggleLike(id,
                    clientHashService.generate(request.getRemoteAddr(), request.getHeader("User-Agent")));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return "redirect:/posts/" + id;
    }

    private static String normalizeKeyword(String q) {
        return q == null ? "" : q.trim();
    }
}
