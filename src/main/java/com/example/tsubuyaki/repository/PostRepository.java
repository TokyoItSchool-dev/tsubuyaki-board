package com.example.tsubuyaki.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Long> {

    List<PostEntity> findTop50ByOrderByCreatedAtDescIdDesc();

    List<PostEntity> findTop50ByBodyContainingOrderByCreatedAtDescIdDesc(String keyword);

    List<PostEntity> findTop50ByTagsNameOrderByCreatedAtDescIdDesc(String tagName);
}
