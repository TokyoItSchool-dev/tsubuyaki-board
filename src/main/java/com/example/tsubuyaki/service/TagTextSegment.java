package com.example.tsubuyaki.service;

public class TagTextSegment {

    private final String text;
    private final boolean tag;

    public TagTextSegment(String text, boolean tag) {
        this.text = text;
        this.tag = tag;
    }

    public String getText() {
        return text;
    }

    public boolean isTag() {
        return tag;
    }
}
