package com.example.tsubuyaki.web.dto;

import com.example.tsubuyaki.domain.AvatarColor;
import com.example.tsubuyaki.web.validation.NotBlankText;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PostForm {

    @NotBlankText(message = "投稿者名を入力してください")
    @Size(max = 30, message = "投稿者名は 30 文字以内で入力してください")
    private String author;

    @NotBlank(message = "アバター色を選択してください")
    @Pattern(regexp = AvatarColor.PATTERN, message = "アバター色を選択してください")
    private String avatarColor = AvatarColor.DEFAULT.name();

    @NotBlankText(message = "本文を入力してください")
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
