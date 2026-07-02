package com.example.tsubuyaki.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.example.tsubuyaki.domain.Post;

public class PostForm {

    @NotBlank(message = "投稿者名を入力してください")
    @Size(max = 30, message = "投稿者名は 30 文字以内で入力してください")
    private String author;

    @NotBlank(message = "本文を入力してください")
    @Size(max = 280, message = "本文は 280 文字以内で入力してください")
    private String body;

    @NotBlank(message = "背景色を選択してください")
    @Pattern(regexp = "[0-9A-Fa-f]{6}", message = "背景色は 6 桁の色コードで選択してください")
    private String color = Post.DEFAULT_COLOR;

    /**
     * 投稿フォームを生成する。
     */
    public PostForm() {
    }

    /**
     * 投稿者名を返す。
     *
     * @return 投稿者名
     */
    public String getAuthor() {
        return author;
    }

    /**
     * 投稿者名を設定する。
     *
     * @param author 投稿者名
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * 本文を返す。
     *
     * @return 本文
     */
    public String getBody() {
        return body;
    }

    /**
     * 本文を設定する。
     *
     * @param body 本文
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * 背景色コードを返す。
     *
     * @return 背景色コード
     */
    public String getColor() {
        return color;
    }

    /**
     * 背景色コードを設定する。
     *
     * @param color 背景色コード
     */
    public void setColor(String color) {
        this.color = color;
    }
}
