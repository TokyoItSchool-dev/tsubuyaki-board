package com.example.tsubuyaki.domain;

import java.util.Arrays;
import java.util.List;

public enum AvatarColor {
    RED,
    BLUE,
    GREEN,
    YELLOW,
    PURPLE,
    ORANGE;

    public static final AvatarColor DEFAULT = BLUE;
    public static final String PATTERN = "RED|BLUE|GREEN|YELLOW|PURPLE|ORANGE";

    public static AvatarColor from(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT;
        }
        try {
            return AvatarColor.valueOf(value.strip());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("avatarColor is invalid: " + value, e);
        }
    }

    public static List<String> names() {
        return Arrays.stream(values())
                .map(AvatarColor::name)
                .toList();
    }

}
