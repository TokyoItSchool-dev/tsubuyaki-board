package com.example.tsubuyaki.service;

import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("Service_latest_投稿がないとき_空リストを返す")
    void latest_returnsEmpty_whenNoPosts() {
        given(postRepository.findTop50ByOrderByCreatedAtDesc()).willReturn(Collections.emptyList());

        assertThat(postService.latest()).isEmpty();
    }
}
