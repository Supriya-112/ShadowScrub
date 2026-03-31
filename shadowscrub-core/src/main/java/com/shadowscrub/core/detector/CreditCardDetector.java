package com.shadowscrub.core.detector;

import com.shadowscrub.core.domain.PIIType;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreditCardDetector implements PIIDetector {

    private static final Pattern CC_PATTERN = 
        Pattern.compile("\\b(?:\\d[ -]*?){13,16}\\b");

    @Override
    public PIIType getType() {
        return PIIType.CREDIT_CARD;
    }

    @Override
    public List<DetectionMatch> findMatches(String input) {
        List<DetectionMatch> matches = new ArrayList<>();
        Matcher matcher = CC_PATTERN.matcher(input);

        while (matcher.find()) {
            String rawValue = matcher.group();
            // Strip non-digits for validation
            String cleanValue = rawValue.replaceAll("[^0-9]", "");

            if (isValidLuhn(cleanValue)) {
                matches.add(new DetectionMatch(
                    matcher.start(),
                    matcher.end(),
                    rawValue,
                    PIIType.CREDIT_CARD
                ));
            }
        }
        return matches;
    }

    private boolean isValidLuhn(String number) {
        int sum = 0;
        boolean alternate = false;
        
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(number.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }
}