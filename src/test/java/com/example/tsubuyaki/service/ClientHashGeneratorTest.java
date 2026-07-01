package com.example.tsubuyaki.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClientHashGeneratorTest {

    private final ClientHashGenerator generator = new ClientHashGenerator();

    @Test
    @DisplayName("clientHash生成_IPとUserAgentを連結してSHA256の先頭8文字を返す")
    void generate_hashesIpAndUserAgentAndReturnsFirst8Characters() {
        String actual = generator.generate("203.0.113.9", "JUnit UA");

        assertThat(actual).isEqualTo("c9d8305e");
    }

    @Test
    @DisplayName("clientHash生成_UserAgentがnullの場合_空文字として扱う")
    void generate_whenUserAgentNull_treatsAsEmpty() {
        String withNullUserAgent = generator.generate("203.0.113.9", null);
        String withEmptyUserAgent = generator.generate("203.0.113.9", "");

        assertThat(withNullUserAgent).isEqualTo(withEmptyUserAgent);
    }
}
