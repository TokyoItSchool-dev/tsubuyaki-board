package com.example.tsubuyaki.sample;

import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.PostReplyRepository;
import com.example.tsubuyaki.repository.TagRepository;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.service.TagParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Service テストの雛形。TDD の見本として残す (削除禁止)。
 *
 * <p>Mockito で Repository をモック化し、Spring を起動せずにテストする。</p>
 */
@ExtendWith(MockitoExtension.class)
class SamplePostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private PostReplyRepository postReplyRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagParser tagParser;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("Service_latest_投稿がないとき_空リストを返す")
    void latest_returnsEmpty_byDefault() {
        given(postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc()).willReturn(Collections.emptyList());

        assertThat(postService.latest()).isEmpty();
    }
}
