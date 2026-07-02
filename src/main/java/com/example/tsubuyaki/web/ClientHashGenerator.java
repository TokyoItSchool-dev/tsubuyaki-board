package com.example.tsubuyaki.web;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

@Component
public class ClientHashGenerator {

    private static final int CLIENT_HASH_LENGTH = 8;

    public String generate(String ipAddress, String userAgent) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String source = Objects.toString(ipAddress, "") + Objects.toString(userAgent, "");
            byte[] hashed = digest.digest(source.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed).substring(0, CLIENT_HASH_LENGTH);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }
}
