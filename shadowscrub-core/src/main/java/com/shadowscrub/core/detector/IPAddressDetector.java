package com.shadowscrub.core.detector;

import com.shadowscrub.core.domain.PIIType;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPAddressDetector implements PIIDetector {

    // IPv4: 0-255 . 0-255 . 0-255 . 0-255
    private static final Pattern IPV4_PATTERN = Pattern.compile(
        "\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b"
    );

    // IPv6: Handles standard and compressed formats
    private static final Pattern IPV6_PATTERN = Pattern.compile(
        "\\b(?:[A-F0-9]{1,4}:){7}[A-F0-9]{1,4}\\b|" +
        "\\b((?:[A-F0-9]{1,4}(?::[A-F0-9]{1,4})*)?)::((?:[A-F0-9]{1,4}(?::[A-F0-9]{1,4})*)?)\\b",
        Pattern.CASE_INSENSITIVE
    );

    @Override
    public PIIType getType() {
        return PIIType.IP_ADDRESS;
    }

    @Override
    public List<DetectionMatch> findMatches(String input) {
        List<DetectionMatch> matches = new ArrayList<>();
        
        findWithPattern(input, IPV4_PATTERN, matches);
        findWithPattern(input, IPV6_PATTERN, matches);
        
        return matches;
    }

    private void findWithPattern(String input, Pattern pattern, List<DetectionMatch> matches) {
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            matches.add(new DetectionMatch(
                matcher.start(), 
                matcher.end(), 
                matcher.group(), 
                PIIType.IP_ADDRESS
            ));
        }
    }
}