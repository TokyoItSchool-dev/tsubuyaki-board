package com.example.tsubuyaki.service;

import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TagParser {

    private static final Pattern TAG_PATTERN = Pattern.compile("#([\\p{L}\\p{N}_]+)");

    public List<String> extractTags(String body) {
        if (body == null || body.isBlank()) {
            return List.of();
        }

        LinkedHashSet<String> tags = new LinkedHashSet<>();
        Matcher matcher = TAG_PATTERN.matcher(body);
        while (matcher.find()) {
            tags.add(matcher.group(1));
        }
        return List.copyOf(tags);
    }
}
