package com.example.tsubuyaki.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class ClientHashService {

    private static final int CLIENT_HASH_LENGTH = 8;

    public String generate(String ipAddress, String userAgent) {
        String source = normalize(ipAddress) + normalize(userAgent);
        try {
            byte[] hashed = MessageDigest.getInstance("SHA-256").digest(source.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed).substring(0, CLIENT_HASH_LENGTH);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value;
    }
}
