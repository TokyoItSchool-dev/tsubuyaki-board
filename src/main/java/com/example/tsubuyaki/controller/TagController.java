package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.service.PostService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class TagController {

    private final PostService postService;

    public TagController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/tags/{name}")
    public String listByTag(@PathVariable String name, Model model) {
        model.addAttribute("posts", postService.findByTagName(name));
        model.addAttribute("q", "");
        model.addAttribute("tagName", name);
        return "posts/list";
    }
}
