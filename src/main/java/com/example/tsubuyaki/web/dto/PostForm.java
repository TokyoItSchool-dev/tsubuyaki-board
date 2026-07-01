package com.example.tsubuyaki.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PostForm {

    @NotNull(message = "投稿者名を入力してください")
    @Pattern(regexp = "(?U).*\\S.*", message = "投稿者名を入力してください")
    @Size(max = 30, message = "投稿者名は 30 文字以内で入力してください")
    private String author;

    @NotNull(message = "本文を入力してください")
    @Pattern(regexp = "(?U).*\\S.*", message = "本文を入力してください")
    @Size(max = 280, message = "本文は 280 文字以内で入力してください")
    private String body;

    public PostForm() {
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
