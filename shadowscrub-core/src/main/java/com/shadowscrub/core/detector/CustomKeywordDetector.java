package com.shadowscrub.core.detector;

import com.shadowscrub.core.domain.PIIType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomKeywordDetector implements PIIDetector {
    private final Pattern keywordPattern;

    public CustomKeywordDetector(Set<String> keywords) {
        // join keywords with | (OR) and use \b for word boundaries
        // to avoid scrubbing "Apple" inside "Applegate"
        String regex = "\\b(" + String.join("|", keywords.stream()
                .map(Pattern::quote).toList()) + ")\\b";
        this.keywordPattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public PIIType getType() {
        return PIIType.CUSTOM;
    }

    @Override
    public List<DetectionMatch> findMatches(String input) {
        List<DetectionMatch> matches = new ArrayList<>();
        if (input == null || input.isBlank()) return matches;
        
        Matcher matcher = keywordPattern.matcher(input);
        while (matcher.find()) {
            matches.add(new DetectionMatch(
                matcher.start(),
                matcher.end(),
                matcher.group(),
                PIIType.CUSTOM
            ));
        }
        return matches;
    }
}