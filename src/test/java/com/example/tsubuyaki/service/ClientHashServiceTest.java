package com.example.tsubuyaki.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClientHashServiceTest {

    @Test
    @DisplayName("clientHash_generate_IPとUserAgentをSHA256化し先頭8文字を返す")
    void clientHash_generate_IPとUserAgentをSHA256化し先頭8文字を返す() {
        ClientHashService service = new ClientHashService();

        String actual = service.generate("192.0.2.10", "JUnit Browser");

        assertThat(actual).isEqualTo("0c8cc4eb");
    }

    @Test
    @DisplayName("clientHash_generate_UserAgentがnullでも8文字のハッシュを返す")
    void clientHash_generate_UserAgentがnullでも8文字のハッシュを返す() {
        ClientHashService service = new ClientHashService();

        String actual = service.generate("192.0.2.10", null);

        assertThat(actual).hasSize(8);
    }
}
