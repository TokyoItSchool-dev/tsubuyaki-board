package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
}
