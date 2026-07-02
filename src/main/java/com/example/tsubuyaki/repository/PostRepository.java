package com.example.tsubuyaki.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<PostEntity, Long> {

    List<PostEntity> findTop50ByDeletedAtIsNullOrderByCreatedAtDescIdDesc();

    List<PostEntity> findTop50ByDeletedAtIsNullAndBodyContainingOrderByCreatedAtDescIdDesc(String keyword);

    List<PostEntity> findTop50ByDeletedAtIsNullAndTagsNameOrderByCreatedAtDescIdDesc(String tagName);

    Optional<PostEntity> findByIdAndDeletedAtIsNull(Long id);
}
