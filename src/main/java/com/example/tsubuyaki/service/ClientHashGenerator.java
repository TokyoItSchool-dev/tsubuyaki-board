package com.example.tsubuyaki.service;

import jakarta.servlet.http.HttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class ClientHashGenerator {

    private ClientHashGenerator() {
    }

    public static String from(HttpServletRequest request) {
        return hash(request.getRemoteAddr(), userAgent(request));
    }

    public static String hash(String ipAddress, String userAgent) {
        String source = ipAddress + ":" + userAgent;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(source.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash, 0, 4);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private static String userAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return "";
        }
        return userAgent;
    }
}
