package com.example.tsubuyaki.domain;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class HashtagText {

    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#([\\p{L}\\p{N}\\p{M}_]+)");
    private static final Pattern SPACES_PATTERN = Pattern.compile("[\\p{Zs}\\t]+");

    private HashtagText() {
    }

    public static List<String> extractTagNames(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        Matcher matcher = HASHTAG_PATTERN.matcher(text);
        LinkedHashSet<String> names = new LinkedHashSet<>();
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return List.copyOf(names);
    }

    public static String removeTags(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        return HASHTAG_PATTERN.matcher(text).replaceAll("")
                .lines()
                .map(HashtagText::normalizeLine)
                .filter(line -> !line.isBlank())
                .collect(Collectors.joining("\n"));
    }

    private static String normalizeLine(String line) {
        return SPACES_PATTERN.matcher(line).replaceAll(" ").strip();
    }
}
