package com.example.tsubuyaki.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class PostForm {

    private static final List<String> AVATAR_COLOR_OPTIONS =
            List.of("Red", "Blue", "Green", "Orange", "Purple", "Gray");

    @NotBlank(message = "投稿者を入力してください")
    private String author;

    @NotBlank(message = "本文を入力してください")
    private String body;

    @NotBlank(message = "アバター色を選択してください")
    private String avatarColor = "Gray";

    public PostForm() {
    }

    public static List<String> avatarColorOptions() {
        return AVATAR_COLOR_OPTIONS;
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

    public String trimmedAuthor() {
        return trim(author);
    }

    public String trimmedBody() {
        return trim(body);
    }

    public String trimmedAvatarColor() {
        return trim(avatarColor);
    }

    public boolean hasAllowedAvatarColor() {
        return AVATAR_COLOR_OPTIONS.contains(trimmedAvatarColor());
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        return value.strip();
    }
}
