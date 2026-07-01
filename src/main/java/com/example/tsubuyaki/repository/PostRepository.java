package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // Spring Data JPA のメソッド名クエリで、createdAt 降順の最新50件を取得する。
    List<Post> findTop50ByOrderByCreatedAtDesc();

    // 本文にキーワードを含む投稿を大文字小文字を区別せず検索し、createdAt 降順の最新50件を取得する。
    List<Post> findTop50ByBodyContainingIgnoreCaseOrderByCreatedAtDesc(String keyword);
}
