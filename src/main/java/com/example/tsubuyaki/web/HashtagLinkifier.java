package com.example.tsubuyaki.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HashtagLinkifier {

    private static final Pattern HASHTAG_PATTERN = Pattern.compile("(?<![A-Za-z0-9_])#([A-Za-z0-9_]+)");

    private HashtagLinkifier() {
    }

    public static List<Segment> linkify(String body) {
        if (body == null || body.isEmpty()) {
            return List.of();
        }

        List<Segment> segments = new ArrayList<>();
        Matcher matcher = HASHTAG_PATTERN.matcher(body);
        int position = 0;
        while (matcher.find()) {
            if (matcher.start() > position) {
                segments.add(Segment.text(body.substring(position, matcher.start())));
            }
            segments.add(Segment.hashtag(
                    matcher.group(),
                    matcher.group(1).toLowerCase(Locale.ROOT)));
            position = matcher.end();
        }
        if (position < body.length()) {
            segments.add(Segment.text(body.substring(position)));
        }
        return List.copyOf(segments);
    }

    public record Segment(String text, boolean hashtag, String tagName) {

        static Segment text(String text) {
            return new Segment(text, false, "");
        }

        static Segment hashtag(String text, String tagName) {
            return new Segment(text, true, tagName);
        }
    }
}
