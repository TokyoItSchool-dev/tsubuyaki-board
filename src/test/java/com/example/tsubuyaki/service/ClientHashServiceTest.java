package com.example.tsubuyaki.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClientHashServiceTest {

    private final ClientHashService clientHashService = new ClientHashService();

    @Test
    @DisplayName("クライアントハッシュ_IPとUA_SHA256先頭8文字を返す")
    void クライアントハッシュ_IPとUA_SHA256先頭8文字を返す() {
        String clientHash = clientHashService.generate("127.0.0.1", "JUnit");

        assertThat(clientHash).isEqualTo("1ad93342");
    }
}
