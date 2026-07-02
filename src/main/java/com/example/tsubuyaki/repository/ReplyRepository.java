package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

    List<Reply> findByPostIdOrderByCreatedAtAscIdAsc(Long postId);

    Optional<Reply> findByIdAndPostId(Long id, Long postId);
}
