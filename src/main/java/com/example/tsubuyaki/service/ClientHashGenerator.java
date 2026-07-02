package com.example.tsubuyaki.service;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class ClientHashGenerator {

    public String generate(String remoteAddr, String userAgent) {
        // null値は空文字として扱い、IPアドレス + User-Agentから安定した識別子を作る。
        String source = valueOrEmpty(remoteAddr) + valueOrEmpty(userAgent);
        try {
            // SHA-256の16進文字列を生成し、仕様どおり先頭8文字をclientHashとして使う。
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(source.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256は標準アルゴリズムなので、利用不能な環境では起動後に明確な例外として扱う。
            throw new IllegalStateException("SHA-256が利用できないためclientHashを生成できません", e);
        }
    }

    private String valueOrEmpty(String value) {
        // HttpServletRequestから取得した値がnullでもハッシュ生成処理を継続できるようにする。
        return value == null ? "" : value;
    }
}
