package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PostService {

    // Service は業務処理の入口として Repository へのアクセスをまとめる。
    private final PostRepository repository;

    public PostService(PostRepository repository) {
        this.repository = repository;
    }

    // 一覧画面に表示するため、Repository から最新50件の投稿を取得する。
    public List<Post> latest() {
        return repository.findTop50ByOrderByCreatedAtDesc();
    }

    // 投稿作成は書き込み処理なので readOnly を外し、Repository.save で INSERT する。
    @Transactional
    public Post create(Post post) {
        return repository.save(post);
    }
}
