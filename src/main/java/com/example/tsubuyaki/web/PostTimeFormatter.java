package com.example.tsubuyaki.web;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component("postTimeFormatter")
public class PostTimeFormatter {

    private static final DateTimeFormatter ABSOLUTE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    private final Clock clock;

    public PostTimeFormatter(Clock clock) {
        this.clock = clock;
    }

    public String format(Instant createdAt) {
        if (createdAt == null) {
            return "";
        }

        ZonedDateTime now = ZonedDateTime.now(clock);
        ZonedDateTime created = createdAt.atZone(clock.getZone());
        Duration elapsed = Duration.between(created, now);
        if (elapsed.isNegative() || elapsed.compareTo(Duration.ofMinutes(1)) < 0) {
            return "たった今";
        }

        LocalDate today = now.toLocalDate();
        LocalDate createdDate = created.toLocalDate();
        if (createdDate.equals(today)) {
            if (elapsed.compareTo(Duration.ofHours(1)) < 0) {
                return elapsed.toMinutes() + "分前";
            }
            return elapsed.toHours() + "時間前";
        }

        if (createdDate.equals(today.minusDays(1))) {
            return "昨日";
        }

        return created.format(ABSOLUTE_FORMATTER);
    }
}
