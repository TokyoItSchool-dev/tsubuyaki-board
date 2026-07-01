package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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

import java.util.List;
import java.util.stream.IntStream;

@Controller
public class PostController {

    private static final String EDITED_POST_ID = "editedPostId";

    private static final String DELETED_POST_ID = "deletedPostId";

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @ModelAttribute
    public void addNoticeIds(Model model, HttpSession session) {
        model.addAttribute(EDITED_POST_ID, session.getAttribute(EDITED_POST_ID));
        model.addAttribute(DELETED_POST_ID, session.getAttribute(DELETED_POST_ID));
    }

    @GetMapping({ "/", "/posts", "/posts/" })
    public String list(@RequestParam(name = "page", defaultValue = "0") int page, Model model) {
        Page<?> postsPage = postService.latestPage(page);
        int totalPages = postsPage.getTotalPages();
        List<Integer> pageNumbers = totalPages == 0
                ? List.of()
                : IntStream.range(0, totalPages).boxed().toList();

        model.addAttribute("posts", postsPage.getContent());
        model.addAttribute("currentPage", postsPage.getNumber());
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasPrevious", postsPage.hasPrevious());
        model.addAttribute("hasNext", postsPage.hasNext());
        model.addAttribute("pageNumbers", pageNumbers);
        return "posts/list";
    }

    @GetMapping("/posts/new")
    public String newForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        model.addAttribute("formTitle", "新規投稿");
        model.addAttribute("formAction", "/posts");
        model.addAttribute("submitLabel", "投稿");
        return "posts/form";
    }

    @PostMapping("/posts")
    public String create(@Valid @ModelAttribute("postForm") PostForm postForm, BindingResult result) {
        if (result.hasErrors()) {
            return "posts/form";
        }

        postService.create(postForm.getAuthor(), postForm.getContent());
        return "redirect:/posts";
    }

    @GetMapping("/posts/trash")
    public String trash(Model model) {
        model.addAttribute("posts", postService.trashedPosts());
        return "posts/trash";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("post", postService.findVisibleById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
        return "posts/detail";
    }

    @GetMapping("/posts/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var post = postService.findVisibleById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        PostForm postForm = new PostForm();
        postForm.setAuthor(post.getAuthor());
        postForm.setBody(post.getBody());
        model.addAttribute("postForm", postForm);
        model.addAttribute("postId", id);
        model.addAttribute("formTitle", "投稿編集");
        model.addAttribute("formAction", "/posts/" + id + "/edit");
        model.addAttribute("submitLabel", "更新");
        return "posts/form";
    }

    @PostMapping("/posts/{id}/edit")
    public String update(@PathVariable Long id,
            @Valid @ModelAttribute("postForm") PostForm postForm,
            BindingResult result,
            Model model,
            HttpSession session) {
        if (result.hasErrors()) {
            model.addAttribute("postId", id);
            model.addAttribute("formTitle", "投稿編集");
            model.addAttribute("formAction", "/posts/" + id + "/edit");
            model.addAttribute("submitLabel", "更新");
            return "posts/form";
        }

        postService.update(id, postForm.getAuthor(), postForm.getContent())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        session.setAttribute(EDITED_POST_ID, id);
        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
        postService.moveToTrash(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        session.setAttribute(DELETED_POST_ID, id);
        return "redirect:/posts";
    }
}
