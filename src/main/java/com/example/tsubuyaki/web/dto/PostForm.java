package com.example.tsubuyaki.web.dto;

import com.example.tsubuyaki.domain.PostBackgroundColor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PostForm {

    @NotBlank(message = "投稿者名を入力してください")
    @Size(max = 30, message = "投稿者名は 30 文字以内で入力してください")
    private String author;

    @NotBlank(message = "本文を入力してください")
    @Size(max = 280, message = "本文は 280 文字以内で入力してください")
    private String body;

    @Pattern(
            regexp = PostBackgroundColor.PATTERN,
            message = "背景色は選択肢から選んでください")
    private String backgroundColor = PostBackgroundColor.DEFAULT;

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

    public String getBackgroundColor() {
        return PostBackgroundColor.normalize(backgroundColor);
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String[] getBackgroundColors() {
        return PostBackgroundColor.colors();
    }
}
