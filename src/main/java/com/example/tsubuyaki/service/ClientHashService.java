package com.example.tsubuyaki.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class ClientHashService {

    private static final int HASH_LENGTH = 8;

    public String generate(String ipAddress, String userAgent) {
        String source = nullToEmpty(ipAddress) + nullToEmpty(userAgent);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(source.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, HASH_LENGTH);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private String nullToEmpty(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }
}
