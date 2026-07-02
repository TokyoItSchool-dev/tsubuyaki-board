/*
 * 認証機能がない状態で、いいね操作の利用者を緩く識別するための
 * クライアントハッシュを生成するサービス。
 */
package com.example.tsubuyaki.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class ClientHashService {

    /**
     * IP アドレスと User-Agent から短い利用者識別子を生成する。
     *
     * <p>個人情報をそのまま保存しないため、SHA-256 の先頭 8 桁だけを
     * いいね判定用のクライアントハッシュとして使う。どちらかの値が
     * {@code null} の場合も空文字として扱い、生成処理を失敗させない。</p>
     *
     * @param ipAddress リクエスト元 IP アドレス
     * @param userAgent User-Agent ヘッダー
     * @return 8 桁のクライアントハッシュ
     */
    public String generate(String ipAddress, String userAgent) {
        String source = valueOrEmpty(ipAddress) + valueOrEmpty(userAgent);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(source.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * ハッシュ元文字列の {@code null} を空文字にそろえる。
     *
     * @param value 入力値
     * @return {@code null} の場合は空文字、それ以外は入力値
     */
    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
