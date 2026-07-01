package com.example.tsubuyaki.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PostForm {

    @NotBlank(message = "投稿者は必須です。")
    @Size(max = 30, message = "投稿者は30文字以内で入力してください。")
    private String author;

    @NotBlank(message = "本文は必須です。")
    @Size(max = 280, message = "本文は280文字以内で入力してください。")
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
