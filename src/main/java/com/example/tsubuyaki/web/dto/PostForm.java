package com.example.tsubuyaki.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PostForm {

    public static final String DEFAULT_AUTHOR_ICON_COLOR = "#2563EB";

    @NotBlank(message = "投稿者名を入力してください")
    @Size(max = 30, message = "投稿者名は 30 文字以内で入力してください")
    private String author;

    @NotBlank(message = "本文を入力してください")
    @Size(max = 280, message = "本文は 280 文字以内で入力してください")
    private String body;

    @NotBlank(message = "アイコン色を選択してください")
    @Pattern(regexp = "#2563EB|#0891B2|#16A34A|#F97316|#DB2777",
            message = "アイコン色は選択肢から選んでください")
    private String authorIconColor = DEFAULT_AUTHOR_ICON_COLOR;

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

    public String getAuthorIconColor() {
        return authorIconColor;
    }

    public void setAuthorIconColor(String authorIconColor) {
        this.authorIconColor = authorIconColor;
    }
}
