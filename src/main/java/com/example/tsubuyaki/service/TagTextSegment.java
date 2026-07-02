package com.example.tsubuyaki.service;

public class TagTextSegment {

    private final String text;
    private final boolean tag;

    /**
     * 本文表示用のセグメントを生成する。
     *
     * @param text 表示する文字列
     * @param tag タグ部分の場合 true
     */
    public TagTextSegment(String text, boolean tag) {
        this.text = text;
        this.tag = tag;
    }

    /**
     * 表示文字列を返す。
     *
     * @return 表示文字列
     */
    public String getText() {
        return text;
    }

    /**
     * タグ部分かどうかを返す。
     *
     * @return タグ部分の場合 true
     */
    public boolean isTag() {
        return tag;
    }
}
