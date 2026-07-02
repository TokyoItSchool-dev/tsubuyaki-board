package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    public String list(@RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "q", required = false) String query,
            Model model) {
        Page<?> postsPage = isSearchableQuery(query)
                ? postService.searchPage(query, page)
                : postService.latestPage(page);
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
        model.addAttribute("searchQuery", query == null ? "" : query);
        return "posts/list";
    }

    @GetMapping("/posts/new")
    public String newForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        addNewFormAttributes(model);
        return "posts/form";
    }

    @PostMapping("/posts")
    public String create(@Valid @ModelAttribute("postForm") PostForm postForm,
            BindingResult result,
            Model model) throws IOException {
        validateAvatar(postForm, result);
        if (result.hasErrors()) {
            addNewFormAttributes(model);
            return "posts/form";
        }

        MultipartFile avatarImage = postForm.getAvatarImage();
        boolean hasAvatarImage = hasAvatarImage(avatarImage);
        postService.create(postForm.getAuthor(), postForm.getContent(),
                hasAvatarImage ? null : normalizedAvatarColor(postForm),
                hasAvatarImage ? avatarImage.getContentType() : null,
                hasAvatarImage ? avatarImage.getBytes() : null);
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
        model.addAttribute("likeCount", postService.likeCount(id));
        return "posts/detail";
    }

    @GetMapping("/posts/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var post = postService.findVisibleById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        PostForm postForm = new PostForm();
        postForm.setAuthor(post.getAuthor());
        postForm.setBody(post.getBody());
        postForm.setAvatarColor(post.getAvatarColor());
        model.addAttribute("postForm", postForm);
        addEditFormAttributes(model, id);
        return "posts/form";
    }

    @PostMapping("/posts/{id}/edit")
    public String update(@PathVariable Long id,
            @Valid @ModelAttribute("postForm") PostForm postForm,
            BindingResult result,
            Model model,
            HttpSession session) throws IOException {
        validateAvatar(postForm, result);
        if (result.hasErrors()) {
            addEditFormAttributes(model, id);
            return "posts/form";
        }

        MultipartFile avatarImage = postForm.getAvatarImage();
        boolean hasAvatarImage = hasAvatarImage(avatarImage);
        postService.update(id, postForm.getAuthor(), postForm.getContent(),
                hasAvatarImage ? null : normalizedAvatarColor(postForm),
                hasAvatarImage ? avatarImage.getContentType() : null,
                hasAvatarImage ? avatarImage.getBytes() : null)
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

    @PostMapping("/posts/{id}/likes")
    public String toggleLike(@PathVariable Long id, HttpServletRequest request) {
        postService.toggleLike(id, clientHash(request))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return "redirect:/posts/" + id;
    }

    private static String clientHash(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent") == null ? "" : request.getHeader("User-Agent");
        return sha256First8(request.getRemoteAddr() + userAgent);
    }

    private static String sha256First8(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean isSearchableQuery(String query) {
        return query != null && !query.strip().isEmpty() && !query.contains("<") && !query.contains(">");
    }

    private static void addNewFormAttributes(Model model) {
        model.addAttribute("formTitle", "新規投稿");
        model.addAttribute("formAction", "/posts");
        model.addAttribute("submitLabel", "投稿");
    }

    private static void addEditFormAttributes(Model model, Long id) {
        model.addAttribute("postId", id);
        model.addAttribute("formTitle", "投稿編集");
        model.addAttribute("formAction", "/posts/" + id + "/edit");
        model.addAttribute("submitLabel", "更新");
    }

    private static void validateAvatar(PostForm postForm, BindingResult result) {
        boolean hasColor = normalizedAvatarColor(postForm) != null;
        boolean hasImage = hasAvatarImage(postForm.getAvatarImage());
        if (hasColor && hasImage) {
            result.rejectValue("avatarColor", "avatar.exclusive", "カラーと画像は同時に選択できません");
        }
        if (!hasColor && !hasImage) {
            result.rejectValue("avatarColor", "avatar.required", "画像をアップロードしない場合はカラーを選択してください");
        }
    }

    private static String normalizedAvatarColor(PostForm postForm) {
        String avatarColor = postForm.getAvatarColor();
        return avatarColor == null || avatarColor.isBlank() ? null : avatarColor;
    }

    private static boolean hasAvatarImage(MultipartFile avatarImage) {
        return avatarImage != null && !avatarImage.isEmpty();
    }
}
