package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Controller
public class PostController {

    private static final List<String> AVATAR_COLORS = List.of("red", "blue", "green", "yellow", "purple");

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping({ "/", "/posts", "/posts/" })
    public String list(@RequestParam(name = "q", required = false) String q,
            Model model) {
        List<Post> posts = StringUtils.hasText(q) ? postService.search(q) : postService.latest();
        setupListModel(posts, model);
        model.addAttribute("q", q == null ? "" : q);
        return "posts/list";
    }

    @GetMapping("/posts/deleted")
    public String deletedList(Model model) {
        model.addAttribute("posts", postService.deleted());
        model.addAttribute("deletedMode", true);
        model.addAttribute("q", "");
        return "posts/list";
    }

    @GetMapping("/tags/{name}")
    public String tagList(@PathVariable String name, Model model) {
        setupListModel(postService.findByTagName(name), model);
        model.addAttribute("q", "");
        model.addAttribute("tagName", name);
        return "posts/list";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Post post = postService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "投稿が見つかりません"));

        model.addAttribute("postId", id);
        model.addAttribute("post", post);
        model.addAttribute("likeCount", postService.countLikes(id));
        model.addAttribute("tags", postService.findTagsByPostId(id));
        return "posts/detail";
    }

    @PostMapping("/posts/{id}/likes")
    public String incrementLike(@PathVariable Long id,
            @RequestParam(name = "redirectTo", required = false) String redirectTo) {
        postService.incrementLike(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "投稿が見つかりません"));
        if ("/posts".equals(redirectTo) || "/posts/".equals(redirectTo)) {
            return "redirect:/posts";
        }
        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts/{id}/delete")
    public String delete(@PathVariable Long id) {
        if (!postService.delete(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "投稿が見つかりません");
        }
        return "redirect:/posts";
    }

    @GetMapping("/posts/new")
    public String newForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        setupCreateForm(model);
        return "posts/form";
    }

    @PostMapping("/posts")
    public String create(@Valid PostForm postForm, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            setupCreateForm(model);
            return "posts/form";
        }

        postService.create(postForm.getAuthor(), postForm.getBody(), postForm.getAvatarColor());
        return "redirect:/posts";
    }

    @GetMapping("/posts/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        PostForm postForm = postService.findById(id)
                .map(post -> {
                    PostForm form = new PostForm();
                    form.setAuthor(post.getAuthor());
                    form.setBody(post.getBody());
                    form.setAvatarColor(post.getAvatarColor());
                    return form;
                })
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "投稿が見つかりません"));

        model.addAttribute("postForm", postForm);
        setupEditForm(model, id);
        return "posts/form";
    }

    @PostMapping("/posts/{id}")
    public String update(@PathVariable Long id,
            @Valid PostForm postForm,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            setupEditForm(model, id);
            return "posts/form";
        }

        postService.update(id, postForm.getAuthor(), postForm.getBody(), postForm.getAvatarColor())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "投稿が見つかりません"));
        return "redirect:/posts/" + id;
    }

    private void setupCreateForm(Model model) {
        model.addAttribute("formTitle", "新規投稿");
        model.addAttribute("formAction", "/posts");
        model.addAttribute("cancelUrl", "/posts");
        model.addAttribute("avatarColors", AVATAR_COLORS);
    }

    private void setupEditForm(Model model, Long id) {
        model.addAttribute("formTitle", "投稿編集");
        model.addAttribute("formAction", "/posts/" + id);
        model.addAttribute("cancelUrl", "/posts/" + id);
        model.addAttribute("avatarColors", AVATAR_COLORS);
    }

    private void setupListModel(List<Post> posts, Model model) {
        List<Long> postIds = posts.stream()
                .map(Post::getId)
                .filter(Objects::nonNull)
                .toList();

        model.addAttribute("posts", posts);
        model.addAttribute("likeCounts", postService.countLikesByPostIds(postIds));
    }
}
