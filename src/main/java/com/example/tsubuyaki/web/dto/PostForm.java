package com.example.tsubuyaki.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class PostForm {

    @NotBlank(message = "投稿者名を入力してください")
    @Pattern(regexp = "^(?=.*[^\\s　]).*$", message = "投稿者名を入力してください")
    @Size(max = 30, message = "投稿者名は 30 文字以内で入力してください")
    private String author;

    @NotBlank(message = "本文を入力してください")
    @Pattern(regexp = "^(?=.*[^\\s　]).*$", message = "本文を入力してください")
    @Size(max = 280, message = "本文は 280 文字以内で入力してください")
    @Pattern(
            regexp = "(?is)^(?!.*<\\s*/?\\s*script\\b).*$",
            message = "本文に script タグは入力できません")
    private String body;

    @Pattern(regexp = "^$|#[0-9a-fA-F]{6}", message = "カラーは #RRGGBB 形式で選択してください")
    private String avatarColor;

    private MultipartFile avatarImage;

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

    public String getContent() {
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

    public MultipartFile getAvatarImage() {
        return avatarImage;
    }

    public void setAvatarImage(MultipartFile avatarImage) {
        this.avatarImage = avatarImage;
    }
}
