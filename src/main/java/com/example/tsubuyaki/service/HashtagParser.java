package com.example.tsubuyaki.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HashtagParser {

    private static final Pattern HASHTAG_PATTERN = Pattern.compile("(?<![A-Za-z0-9_])#([A-Za-z0-9_]+)");

    public List<String> parse(String body) {
        if (body == null || body.isBlank()) {
            return List.of();
        }

        Set<String> tags = new LinkedHashSet<>();
        Matcher matcher = HASHTAG_PATTERN.matcher(body);
        while (matcher.find()) {
            tags.add(matcher.group(1).toLowerCase(Locale.ROOT));
        }
        return List.copyOf(tags);
    }
}
