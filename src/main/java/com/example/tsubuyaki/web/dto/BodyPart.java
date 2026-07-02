package com.example.tsubuyaki.web.dto;

public class BodyPart {

    private final String text;
    private final String tagName;

    private BodyPart(String text, String tagName) {
        this.text = text;
        this.tagName = tagName;
    }

    public static BodyPart text(String text) {
        return new BodyPart(text, null);
    }

    public static BodyPart tag(String text, String tagName) {
        return new BodyPart(text, tagName);
    }

    public String getText() {
        return text;
    }

    public String getTagName() {
        return tagName;
    }

    public boolean isTag() {
        return tagName != null;
    }
}
