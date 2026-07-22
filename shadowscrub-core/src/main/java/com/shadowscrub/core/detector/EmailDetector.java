package com.shadowscrub.core.detector;

import com.shadowscrub.core.domain.PIIType;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects email addresses. The pattern is a pragmatic subset of RFC 5322:
 * it accepts the local/domain characters seen in real-world addresses without
 * trying to model the full grammar, which in practice matches more spam-list
 * edge cases than it rejects.
 */
public class EmailDetector implements PIIDetector {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b"
    );

    @Override
    public PIIType getType() {
        return PIIType.EMAIL;
    }

    @Override
    public List<DetectionMatch> findMatches(String input) {
        List<DetectionMatch> matches = new ArrayList<>();
        if (input == null || input.isBlank()) {
            return matches;
        }
        Matcher matcher = EMAIL_PATTERN.matcher(input);
        while (matcher.find()) {
            matches.add(new DetectionMatch(
                matcher.start(),
                matcher.end(),
                matcher.group(),
                PIIType.EMAIL
            ));
        }
        return matches;
    }
}
