package com.example.tsubuyaki.web.dto;

import com.example.tsubuyaki.domain.Post;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class PostForm {

    // 投稿者は必須入力。空文字や空白だけの場合はエラーメッセージを表示する。
    @NotBlank(message = "投稿者を入力してください")
    // DB の author カラム長に合わせ、投稿者名は30文字までに制限する。
    @Size(max = 30, message = "投稿者名は 30 文字以内で入力してください")
    private String author;

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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    // Controller から渡された作成日時を使って、保存対象の Post エンティティへ変換する。
    public Post toPost(Instant createdAt) {
        return new Post(author, body, createdAt);
    }
}
