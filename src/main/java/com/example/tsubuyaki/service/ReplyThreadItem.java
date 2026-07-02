package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Reply;

public record ReplyThreadItem(Reply reply, int depth) {
}
