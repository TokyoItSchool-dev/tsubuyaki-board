package com.example.tsubuyaki.sample;

import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Service テストの雛形。TDD の見本として残す (削除禁止)。
 *
 * <p>Mockito で Repository をモック化し、Spring を起動せずにテストする。</p>
 */
@ExtendWith(MockitoExtension.class)
class SamplePostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("Service_latest50_Repositoryが空のとき_空リストを返す")
    void latest50_returnsEmptyWhenRepositoryReturnsEmpty() {
        assertThat(postService.latest50()).isEmpty();
    }
}
