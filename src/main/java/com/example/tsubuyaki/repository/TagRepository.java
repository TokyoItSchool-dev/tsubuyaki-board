package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByNameOrderByPostCreatedAtDesc(String name);

    @Query("select distinct t.post from Tag t where t.name = :name order by t.post.createdAt desc")
    List<Post> findPostsByNameOrderByCreatedAtDesc(@Param("name") String name);

    @Query("select distinct t.post from Tag t "
            + "where t.name like concat(concat('%', :keyword), '%') "
            + "order by t.post.createdAt desc")
    List<Post> findPostsByNameContainingOrderByCreatedAtDesc(@Param("keyword") String keyword);
}
