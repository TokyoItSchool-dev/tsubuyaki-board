package com.example.tsubuyaki.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PostForm {

    private static final String DEFAULT_AVATAR_COLOR = "gray";
    private static final String AVATAR_COLOR_PATTERN = "^(|red|blue|green|yellow|gray)$";

    @NotBlank(message = "投稿者名を入力してください")
    @Size(max = 30, message = "投稿者名は 30 文字以内で入力してください")
    private String author;

    @NotBlank(message = "本文を入力してください")
    @Size(max = 280, message = "本文は 280 文字以内で入力してください")
    private String body;

    @Pattern(regexp = AVATAR_COLOR_PATTERN, message = "アバター色は選択肢から選んでください")
    private String avatarColor;

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

    public String getAvatarColor() {
        return avatarColor;
    }

    public void setAvatarColor(String avatarColor) {
        this.avatarColor = avatarColor;
    }

    public String avatarColorOrDefault() {
        if (avatarColor == null || avatarColor.isBlank()) {
            return DEFAULT_AVATAR_COLOR;
        }
        return avatarColor;
    }
}
