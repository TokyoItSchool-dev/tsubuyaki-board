package com.example.tsubuyaki.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClientHashGeneratorTest {

    private final ClientHashGenerator clientHashGenerator = new ClientHashGenerator();

    @Test
    @DisplayName("clientHash生成_IPアドレスとUA_SHA256先頭8文字を返す")
    void clientHash生成_IPアドレスとUA_SHA256先頭8文字を返す() {
        // IPアドレス + User-AgentをSHA-256化した先頭8文字が返ることを検証する。
        assertThat(clientHashGenerator.generate("192.0.2.10", "TsubuyakiTest/1.0")).isEqualTo("971f6926");
        // IPアドレスが異なると別のclientHashになることを検証する。
        assertThat(clientHashGenerator.generate("192.0.2.20", "TsubuyakiTest/1.0")).isEqualTo("e6463160");
        // User-Agentが異なると別のclientHashになることを検証する。
        assertThat(clientHashGenerator.generate("192.0.2.10", "AnotherBrowser/2.0")).isEqualTo("19a49c44");
    }
}
