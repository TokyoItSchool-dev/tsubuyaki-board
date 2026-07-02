package com.example.tsubuyaki.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;

class ClientHashGeneratorTest {

    @Test
    @DisplayName("いいね_clientHash_IPとUserAgentからSHA256先頭8文字を生成する")
    void generate_whenIpAndUserAgentGiven_returnsFirst8CharsOfSha256() throws NoSuchAlgorithmException {
        ClientHashGenerator generator = new ClientHashGenerator();

        String actual = generator.generate("203.0.113.10", "JUnit UA");

        assertThat(actual).isEqualTo(expectedHash("203.0.113.10" + "JUnit UA"));
    }

    private static String expectedHash(String source) throws NoSuchAlgorithmException {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(source.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte value : digest) {
            hex.append(String.format("%02x", value));
        }
        return hex.substring(0, 8);
    }
}
