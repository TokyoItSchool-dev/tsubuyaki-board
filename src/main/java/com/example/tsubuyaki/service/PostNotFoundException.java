package com.example.tsubuyaki.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// 投稿詳細で指定されたidがDBに存在しない場合、HTTP 404としてブラウザへ返すための例外。
@ResponseStatus(HttpStatus.NOT_FOUND)
public class PostNotFoundException extends RuntimeException {
}
