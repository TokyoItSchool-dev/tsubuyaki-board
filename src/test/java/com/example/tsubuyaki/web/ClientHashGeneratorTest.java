package com.example.tsubuyaki.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClientHashGeneratorTest {

    private final ClientHashGenerator generator = new ClientHashGenerator();

    @Test
    @DisplayName("clientHash生成_IPとUserAgent_SHA256先頭8文字を返す")
    void clientHash生成_IPとUserAgent_SHA256先頭8文字を返す() {
        String clientHash = generator.generate("192.0.2.1", "Mozilla/5.0");

        assertThat(clientHash).isEqualTo("c44b2068");
    }

    @Test
    @DisplayName("clientHash生成_UserAgent未指定_空文字としてSHA256先頭8文字を返す")
    void clientHash生成_UserAgent未指定_空文字としてSHA256先頭8文字を返す() {
        String clientHash = generator.generate("192.0.2.1", null);

        assertThat(clientHash).isEqualTo("37fcff24");
    }
}
