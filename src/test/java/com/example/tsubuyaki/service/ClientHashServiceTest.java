package com.example.tsubuyaki.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClientHashServiceTest {

    private final ClientHashService clientHashService = new ClientHashService();

    @Test
    @DisplayName("clientHash_IPとUserAgent_SHA256の先頭8文字を返す")
    void clientHash_IPとUserAgent_SHA256の先頭8文字を返す() {
        String clientHash = clientHashService.generate("203.0.113.10", "JUnit");

        assertThat(clientHash).isEqualTo("829dd182");
    }
}
