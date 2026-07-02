package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.service.ClientHashGenerator;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import com.example.tsubuyaki.web.dto.PostView;
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

import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
public class PostController {

    private static final String RETURN_TO_LIST = "list";
    private static final String RETURN_TO_DETAIL = "detail";
    private static final String DELETE_ERROR_MESSAGE = "この投稿は削除できません。";
    private static final String NOT_FOUND_MESSAGE = "投稿が見つかりません。";

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
    public String create(
            @Valid @ModelAttribute("postForm") PostForm postForm,
            BindingResult bindingResult,
            HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return "posts/form";
        }
        postService.create(
                postForm.getAuthor(),
                postForm.getBody(),
                postForm.getBackgroundColor(),
                clientHash(request));
        return "redirect:/posts";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, HttpServletRequest request, Model model) {
        return showDetail(id, clientHash(request), model, null);
    }

    @GetMapping("/posts/{id}/delete-confirm")
    public String deleteConfirm(@PathVariable Long id, HttpServletRequest request, Model model) {
        String clientHash = clientHash(request);
        Optional<PostView> post = postService.findById(id, clientHash);
        if (post.isEmpty()) {
            return showDetail(id, clientHash, model, null);
        }
        if (!post.get().isCanDelete()) {
            model.addAttribute("post", post.get());
            model.addAttribute("detailError", DELETE_ERROR_MESSAGE);
            return "posts/detail";
        }
        model.addAttribute("post", post.get());
        return "posts/delete-confirm";
    }

    @PostMapping("/posts/{id}/del")
    public String delete(@PathVariable Long id, HttpServletRequest request, Model model) {
        String clientHash = clientHash(request);
        if (postService.delete(id, clientHash)) {
            return "redirect:/posts";
        }
        return showDetail(id, clientHash, model, DELETE_ERROR_MESSAGE);
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

    private String showDetail(Long id, String clientHash, Model model, String errorMessage) {
        Optional<PostView> post = postService.findById(id, clientHash);
        if (post.isPresent()) {
            model.addAttribute("post", post.get());
            if (errorMessage != null) {
                model.addAttribute("detailError", errorMessage);
            }
            return "posts/detail";
        }
        if (postService.existsDeletedById(id)) {
            model.addAttribute("detailError", NOT_FOUND_MESSAGE);
            return "posts/detail";
        }
        throw new ResponseStatusException(NOT_FOUND);
    }

    private String normalizeQuery(String query) {
        if (query == null) {
            return "";
        }
        return query.trim();
    }

    private String clientHash(HttpServletRequest request) {
        return ClientHashGenerator.from(request);
    }
}
