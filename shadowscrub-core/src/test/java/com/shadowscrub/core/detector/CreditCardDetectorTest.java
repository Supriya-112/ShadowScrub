package com.shadowscrub.core.detector;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CreditCardDetectorTest {

    private final CreditCardDetector detector = new CreditCardDetector();

    @Test
    void shouldDetectValidCreditCard() {
        String input = "Pay with card 4242 4242 4242 4242";
        var matches = detector.findMatches(input);
        
        assertThat(matches).hasSize(1);
        assertThat(matches.get(0).value()).contains("4242");
    }

    @Test
    void shouldIgnoreInvalidCardNumber() {
        // This looks like a card but fails the Luhn check
        String input = "Serial number: 1234-5678-9012-3456";
        var matches = detector.findMatches(input);
        
        assertThat(matches).isEmpty();
    }
}