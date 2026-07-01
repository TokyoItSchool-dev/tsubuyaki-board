package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.ClientHashGenerator;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import jakarta.servlet.http.HttpServletRequest;
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

@Controller
public class PostController {

    private final PostService postService;
    private final ClientHashGenerator clientHashGenerator = new ClientHashGenerator();

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping({ "/", "/posts", "/posts/" })
    public String list(@RequestParam(name = "q", required = false) String q, Model model) {
        model.addAttribute("posts", StringUtils.hasText(q) ? postService.search(q) : postService.latest());
        model.addAttribute("q", q == null ? "" : q);
        return "posts/list";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model, HttpServletRequest request) {
        Post post = postService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "投稿が見つかりません"));
        String clientHash = clientHash(request);

        model.addAttribute("postId", id);
        model.addAttribute("post", post);
        model.addAttribute("likeCount", postService.countLikes(id));
        model.addAttribute("likedByClient", postService.likedBy(id, clientHash));
        return "posts/detail";
    }

    @PostMapping("/posts/{id}/likes")
    public String toggleLike(@PathVariable Long id, HttpServletRequest request) {
        postService.toggleLike(id, clientHash(request))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "投稿が見つかりません"));
        return "redirect:/posts/" + id;
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

        postService.create(postForm.getAuthor(), postForm.getBody());
        return "redirect:/posts";
    }

    @GetMapping("/posts/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        PostForm postForm = postService.findById(id)
                .map(post -> {
                    PostForm form = new PostForm();
                    form.setAuthor(post.getAuthor());
                    form.setBody(post.getBody());
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

        postService.update(id, postForm.getAuthor(), postForm.getBody())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "投稿が見つかりません"));
        return "redirect:/posts/" + id;
    }

    private void setupCreateForm(Model model) {
        model.addAttribute("formTitle", "新規投稿");
        model.addAttribute("formAction", "/posts");
        model.addAttribute("cancelUrl", "/posts");
    }

    private void setupEditForm(Model model, Long id) {
        model.addAttribute("formTitle", "投稿編集");
        model.addAttribute("formAction", "/posts/" + id);
        model.addAttribute("cancelUrl", "/posts/" + id);
    }

    private String clientHash(HttpServletRequest request) {
        return clientHashGenerator.generate(request.getRemoteAddr(), request.getHeader("User-Agent"));
    }
}
