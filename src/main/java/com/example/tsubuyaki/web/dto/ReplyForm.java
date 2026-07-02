package com.example.tsubuyaki.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReplyForm {

    private Long parentReplyId;

    @NotBlank(message = "ユーザー名を入力してください")
    @Size(max = 15, message = "ユーザー名は 15 文字以内で入力してください")
    private String author;

    @NotBlank(message = "コメントを入力してください")
    @Size(max = 1000, message = "コメントは 1000 文字以内で入力してください")
    private String body;

    public ReplyForm() {
    }

    public Long getParentReplyId() {
        return parentReplyId;
    }

    public void setParentReplyId(Long parentReplyId) {
        this.parentReplyId = parentReplyId;
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
}
