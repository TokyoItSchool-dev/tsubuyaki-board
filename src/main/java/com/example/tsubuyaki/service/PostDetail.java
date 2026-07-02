package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;

public record PostDetail(Post post, long likeCount, boolean liked) {
}
