package com.example.tsubuyaki.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClientHashServiceTest {

    private final ClientHashService clientHashService = new ClientHashService();

    @Test
    @DisplayName("clientHash生成_IPアドレスとUserAgentからSHA256先頭8文字を返す")
    void clientHash生成_IPアドレスとUserAgentからSHA256先頭8文字を返す() {
        String clientHash = clientHashService.hash("192.0.2.1", "JUnit");

        assertThat(clientHash).isEqualTo("73c89905");
    }
}
