package com.example.tsubuyaki.service;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagExtractor {

    private static final Pattern TAG_PATTERN = Pattern.compile("#([\\p{L}\\p{N}_-]+)");

    public Set<String> extract(String body) {
        Set<String> names = new LinkedHashSet<>();
        if (body == null) {
            return names;
        }

        Matcher matcher = TAG_PATTERN.matcher(body);
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }
}
