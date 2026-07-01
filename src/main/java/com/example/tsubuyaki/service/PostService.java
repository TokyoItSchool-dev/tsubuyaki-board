package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.web.dto.PostForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository repository;

    public PostService(PostRepository repository) {
        this.repository = repository;
    }

    public List<Post> latest() {
        return repository.findTop50ByOrderByCreatedAtDesc();
    }

    public Post findById(Long id) {
        // 詳細画面では1件だけ取得し、存在しないidの場合はControllerへ404用の例外を伝える。
        return repository.findById(id).orElseThrow(PostNotFoundException::new);
    }

    @Transactional
    public Post create(PostForm form) {
        return repository.save(new Post(form.getAuthor(), form.getBody(), LocalDateTime.now()));
    }
}
