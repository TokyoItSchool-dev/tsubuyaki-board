package com.example.tsubuyaki.service;

import com.example.tsubuyaki.web.dto.BodyPart;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TagParser {

    private static final Pattern TAG_PATTERN = Pattern.compile("#([\\p{L}\\p{N}_][\\p{L}\\p{N}_-]{0,49})");

    public List<String> extractNames(String body) {
        Set<String> names = new LinkedHashSet<>();
        if (body == null || body.isBlank()) {
            return List.of();
        }
        Matcher matcher = TAG_PATTERN.matcher(body);
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return List.copyOf(names);
    }

    public List<BodyPart> bodyParts(String body) {
        if (body == null || body.isEmpty()) {
            return List.of();
        }
        List<BodyPart> parts = new ArrayList<>();
        Matcher matcher = TAG_PATTERN.matcher(body);
        int cursor = 0;
        while (matcher.find()) {
            if (cursor < matcher.start()) {
                parts.add(BodyPart.text(body.substring(cursor, matcher.start())));
            }
            parts.add(BodyPart.tag(matcher.group(), matcher.group(1)));
            cursor = matcher.end();
        }
        if (cursor < body.length()) {
            parts.add(BodyPart.text(body.substring(cursor)));
        }
        return parts;
    }
}
