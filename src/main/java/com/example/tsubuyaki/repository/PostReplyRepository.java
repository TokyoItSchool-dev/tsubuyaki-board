package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.PostReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostReplyRepository extends JpaRepository<PostReply, Long> {

    List<PostReply> findByPostIdOrderByCreatedAtAsc(Long postId);

    Optional<PostReply> findByIdAndPostId(Long id, Long postId);

    long countByPostId(Long postId);
}
