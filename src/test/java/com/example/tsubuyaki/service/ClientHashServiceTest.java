package com.example.tsubuyaki.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ClientHashServiceTest {

    private final ClientHashService clientHashService = new ClientHashService();

    @Test
    @DisplayName("clientHash生成_IPとUserAgentをSHA256化し先頭8文字を返す")
    void from_returnsFirst8CharsOfSha256ForIpAndUserAgent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("User-Agent", "Mozilla/5.0");

        String actual = clientHashService.from(request);

        assertThat(actual).isEqualTo("c404b6c9");
    }

    @Test
    @DisplayName("clientHash生成_UserAgentがない場合_空文字として扱う")
    void from_whenUserAgentIsMissing_usesEmptyUserAgent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");

        String actual = clientHashService.from(request);

        assertThat(actual).isEqualTo("12ca17b4");
    }
}
