package com.example.tsubuyaki.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PostForm {

    private static final String DEFAULT_AVATAR_COLOR = "red";

    @NotBlank(message = "投稿者名を入力してください")
    @Size(max = 15, message = "投稿者名は 15 文字以内で入力してください")
    private String author;

    private String avatarColor = DEFAULT_AVATAR_COLOR;

    @NotBlank(message = "本文を入力してください")
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

    public String getAvatarColor() {
        if (avatarColor == null || avatarColor.isBlank()) {
            return DEFAULT_AVATAR_COLOR;
        }
        return avatarColor;
    }

    public void setAvatarColor(String avatarColor) {
        this.avatarColor = avatarColor;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
