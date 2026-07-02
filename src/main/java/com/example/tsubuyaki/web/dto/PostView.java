package com.example.tsubuyaki.web.dto;

import com.example.tsubuyaki.domain.Post;

import java.time.Instant;

public class PostView {

    private final Post post;
    private final long likeCount;
    private final boolean likedByCurrentClient;

    public PostView(Post post, long likeCount) {
        this(post, likeCount, false);
    }

    public PostView(Post post, long likeCount, boolean likedByCurrentClient) {
        this.post = post;
        this.likeCount = likeCount;
        this.likedByCurrentClient = likedByCurrentClient;
    }

    public Post getPost() {
        return post;
    }

    public Long getId() {
        return post.getId();
    }

    public String getAuthor() {
        return post.getAuthor();
    }

    public String getBody() {
        return post.getBody();
    }

    public Instant getCreatedAt() {
        return post.getCreatedAt();
    }

    public long getLikeCount() {
        return likeCount;
    }

    public boolean isLikedByCurrentClient() {
        return likedByCurrentClient;
    }
}
