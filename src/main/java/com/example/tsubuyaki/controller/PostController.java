package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.BodyPart;
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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping({ "/", "/posts", "/posts/" })
    public String list(@RequestParam(name = "q", required = false) String query, Model model) {
        List<Post> posts = postService.search(query);
        populateListModel(posts, model);
        model.addAttribute("q", query);
        model.addAttribute("searched", postService.hasSearchQuery(query));
        return "posts/list";
    }

    @GetMapping("/tags/{name}")
    public String postsByTag(@PathVariable String name, Model model) {
        List<Post> posts = postService.postsByTag(name);
        populateListModel(posts, model);
        model.addAttribute("q", "#" + name);
        model.addAttribute("searched", true);
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
        postService.create(postForm.getAuthor(), postForm.getBody(), postForm.avatarColorOrDefault());
        return "redirect:/posts";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model) {
        populateDetailModel(id, model);
        return "posts/detail";
    }

    @PostMapping("/posts/{id}/likes")
    public String like(@PathVariable Long id, HttpServletRequest request) {
        if (!postService.toggleLike(id, clientHash(request))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts/{id}/delete")
    public String delete(@PathVariable Long id) {
        if (!postService.delete(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return "redirect:/posts";
    }

    @PostMapping("/posts/{id}/replies")
    public String createReply(
            @PathVariable Long id,
            @Valid @ModelAttribute("replyForm") PostForm replyForm,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            populateDetailModel(id, model);
            return "posts/detail";
        }
        if (!postService.createReply(id, replyForm.getAuthor(), replyForm.getBody(), replyForm.avatarColorOrDefault())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts/{postId}/replies/{replyId}/delete")
    public String deleteReply(@PathVariable Long postId, @PathVariable Long replyId) {
        if (!postService.deleteReply(postId, replyId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return "redirect:/posts/" + postId;
    }

    private static String clientHash(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String source = request.getRemoteAddr() + (userAgent == null ? "" : userAgent);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(source.getBytes(StandardCharsets.UTF_8))).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }

    private Map<Long, Long> likeCounts(List<Post> posts) {
        Map<Long, Long> likeCounts = new LinkedHashMap<>();
        for (Post post : posts) {
            if (post.getId() != null) {
                likeCounts.put(post.getId(), postService.countLikes(post.getId()));
            }
        }
        return likeCounts;
    }

    private Map<Long, Long> replyCounts(List<Post> posts) {
        Map<Long, Long> replyCounts = new LinkedHashMap<>();
        for (Post post : posts) {
            if (post.getId() != null) {
                replyCounts.put(post.getId(), postService.countReplies(post.getId()));
            }
        }
        return replyCounts;
    }

    private void populateListModel(List<Post> posts, Model model) {
        model.addAttribute("posts", posts);
        model.addAttribute("likeCounts", likeCounts(posts));
        model.addAttribute("replyCounts", replyCounts(posts));
        model.addAttribute("bodyPartsByPostId", bodyPartsByPostId(posts));
    }

    private Map<Long, List<BodyPart>> bodyPartsByPostId(List<Post> posts) {
        Map<Long, List<BodyPart>> bodyPartsByPostId = new LinkedHashMap<>();
        for (Post post : posts) {
            if (post.getId() != null) {
                bodyPartsByPostId.put(post.getId(), bodyParts(post));
            }
        }
        return bodyPartsByPostId;
    }

    private void populateDetailModel(Long id, Model model) {
        Post post = postService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("post", post);
        model.addAttribute("postId", id);
        model.addAttribute("bodyParts", bodyParts(post));
        model.addAttribute("likeCount", postService.countLikes(id));
        model.addAttribute("replies", postService.repliesForPost(id));
        if (!model.containsAttribute("replyForm")) {
            model.addAttribute("replyForm", new PostForm());
        }
    }

    private List<BodyPart> bodyParts(Post post) {
        List<BodyPart> bodyParts = postService.bodyParts(post.getBody());
        if (bodyParts == null || bodyParts.isEmpty()) {
            return List.of(BodyPart.text(post.getBody()));
        }
        return bodyParts;
    }
}
