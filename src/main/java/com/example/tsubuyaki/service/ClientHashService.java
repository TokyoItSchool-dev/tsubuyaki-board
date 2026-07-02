package com.example.tsubuyaki.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class ClientHashService {

    private static final String USER_AGENT = "User-Agent";
    private static final String SHA_256 = "SHA-256";
    private static final int HASH_LENGTH = 8;

    public String from(HttpServletRequest request) {
        String source = request.getRemoteAddr() + userAgent(request);
        return HexFormat.of().formatHex(sha256(source)).substring(0, HASH_LENGTH);
    }

    private String userAgent(HttpServletRequest request) {
        String userAgent = request.getHeader(USER_AGENT);
        if (userAgent == null) {
            return "";
        }
        return userAgent;
    }

    private byte[] sha256(String source) {
        try {
            return MessageDigest.getInstance(SHA_256).digest(source.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 が利用できません", e);
        }
    }
}
