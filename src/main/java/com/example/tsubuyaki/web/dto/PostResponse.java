package com.example.tsubuyaki.web.dto;

import java.time.Instant;

/**
 * 投稿 API で公開する投稿情報。
 *
 * @param id 投稿 ID
 * @param author 投稿者名
 * @param body 本文
 * @param createdAt 投稿日時
 * @param color 背景色コード
 */
public record PostResponse(Long id, String author, String body, Instant createdAt, String color) {
}
