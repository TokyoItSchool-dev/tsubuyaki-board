package com.example.tsubuyaki.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class ClientHashGenerator {

    private static final int HASH_LENGTH = 8;

    public String generate(String ipAddress, String userAgent) {
        String source = nullToEmpty(ipAddress) + nullToEmpty(userAgent);
        byte[] digest = sha256().digest(source.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest).substring(0, HASH_LENGTH);
    }

    private MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }

    private String nullToEmpty(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }
}
