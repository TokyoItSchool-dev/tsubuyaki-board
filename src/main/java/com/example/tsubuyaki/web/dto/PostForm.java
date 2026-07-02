package com.example.tsubuyaki.web.dto;

import com.example.tsubuyaki.validation.NotBlankText;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PostForm {

    @NotBlankText(message = "投稿者は必須です。")
    @Size(max = 30, message = "投稿者は30文字以内で入力してください。")
    private String author;

    @NotBlankText(message = "本文は必須です。")
    @Size(max = 280, message = "本文は280文字以内で入力してください。")
    private String body;

    @Pattern(regexp = "red|blue|yellow|", message = "アバターカラーは赤・青・黄色から選択してください。")
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
}
