package com.example.tsubuyaki.web.dto;

import com.example.tsubuyaki.domain.Post;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class PostForm {

    // 投稿者は必須入力。空文字や空白だけの場合はエラーメッセージを表示する。
    @NotBlank(message = "投稿者を入力してください")
    // DB の author カラム長に合わせ、投稿者名は30文字までに制限する。
    @Size(max = 30, message = "投稿者名は 30 文字以内で入力してください")
    private String author;

    // 投稿者名の左に表示する色。未選択時は灰色を使う。
    @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "投稿者カラーを選択してください")
    private String authorColor = Post.DEFAULT_AUTHOR_COLOR;

    // 本文は必須入力。空文字や空白だけの場合はエラーメッセージを表示する。
    @NotBlank(message = "本文を入力してください")
    // DB の body カラム長に合わせ、本文は280文字までに制限する。
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

    public String getAuthorColor() {
        return authorColor;
    }

    public void setAuthorColor(String authorColor) {
        this.authorColor = authorColor;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    // Controller から渡された作成日時を使って、保存対象の Post エンティティへ変換する。
    public Post toPost(LocalDateTime createdAt) {
        return new Post(author, body, authorColor, createdAt);
    }
}
