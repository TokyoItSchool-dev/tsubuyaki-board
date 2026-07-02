package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.service.ClientHashService;
import com.example.tsubuyaki.service.LikeService;
import com.example.tsubuyaki.service.PostDetail;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import com.example.tsubuyaki.web.dto.PostFormOptions;
import com.example.tsubuyaki.web.mapper.PostMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class PostController {

    private final PostService postService;
    private final LikeService likeService;
    private final ClientHashService clientHashService;
    private final PostFormOptions postFormOptions;

    public PostController(
            PostService postService,
            LikeService likeService,
            ClientHashService clientHashService,
            PostFormOptions postFormOptions) {
        this.postService = postService;
        this.likeService = likeService;
        this.clientHashService = clientHashService;
        this.postFormOptions = postFormOptions;
    }

    @GetMapping({ "/", "/posts", "/posts/" })
    public String list(@RequestParam(name = "q", required = false) String query, Model model) {
        model.addAttribute("posts", PostMapper.toResponseList(postService.findPosts(query)));
        model.addAttribute("query", query == null ? "" : query);
        return "posts/list";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model) {
        PostDetail detail = postService.getDetail(id);
        model.addAttribute("post", PostMapper.toResponse(detail.post()));
        model.addAttribute("likeCount", detail.likeCount());
        return "posts/detail";
    }

    @GetMapping("/posts/new")
    public String newForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        return "posts/form";
    }

    @PostMapping("/posts")
    public String create(@Valid @ModelAttribute("postForm") PostForm postForm, BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            postService.create(postForm.getAuthor(), postForm.getAvatarColor(), postForm.getBody());
            return "redirect:/posts";
        }
        return "posts/form";
    }

    @ModelAttribute("avatarColors")
    public List<String> avatarColors() {
        return postFormOptions.avatarColors();
    }

    @PostMapping("/posts/{id}/likes")
    public String toggleLike(@PathVariable Long id, HttpServletRequest request) {
        String clientHash = clientHashService.generate(
                request.getRemoteAddr(),
                request.getHeader("User-Agent"));
        likeService.toggleLike(id, clientHash);
        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts/{id}/delete")
    public String delete(@PathVariable Long id) {
        postService.delete(id);
        return "redirect:/posts";
    }
}
