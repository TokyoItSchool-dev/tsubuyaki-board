package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {

    List<PostTag> findTop50ByTagNameOrderByPostCreatedAtDesc(String name);
}
