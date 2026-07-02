package com.example.tsubuyaki.domain;

import java.util.Arrays;

public final class PostBackgroundColor {

    public static final String DEFAULT = "#ffffff";
    public static final String PATTERN = "#ffffff|#fee2e2|#f3e8ff|#dbeafe|#dcfce7|#fef9c3";

    private static final String[] COLORS = {
        DEFAULT,
        "#fee2e2",
        "#f3e8ff",
        "#dbeafe",
        "#dcfce7",
        "#fef9c3"
    };

    private static final String DEFAULT_CLASS = "post--bg-default";

    private PostBackgroundColor() {
    }

    public static String normalize(String color) {
        if (isAllowed(color)) {
            return color;
        }
        return DEFAULT;
    }

    public static boolean isAllowed(String color) {
        return Arrays.asList(COLORS).contains(color);
    }

    public static String[] colors() {
        return COLORS.clone();
    }

    public static String cssClass(String color) {
        return switch (normalize(color)) {
            case "#fee2e2" -> "post--bg-red";
            case "#f3e8ff" -> "post--bg-purple";
            case "#dbeafe" -> "post--bg-blue";
            case "#dcfce7" -> "post--bg-green";
            case "#fef9c3" -> "post--bg-yellow";
            default -> DEFAULT_CLASS;
        };
    }
}
