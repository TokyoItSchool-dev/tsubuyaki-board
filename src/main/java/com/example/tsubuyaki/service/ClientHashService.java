package com.example.tsubuyaki.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

@Service
public class ClientHashService {

    private static final int CLIENT_HASH_LENGTH = 8;

    public String hash(String remoteAddress, String userAgent) {
        byte[] digest = sha256(
                Objects.toString(remoteAddress, "") + Objects.toString(userAgent, ""));
        return HexFormat.of().formatHex(digest).substring(0, CLIENT_HASH_LENGTH);
    }

    private byte[] sha256(String source) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(source.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }
}
