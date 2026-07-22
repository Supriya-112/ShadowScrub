package com.shadowscrub.core.detector;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CustomKeywordDetectorTest {

    @Test
    void matchesWholeWordsCaseInsensitively() {
        var detector = new CustomKeywordDetector(Set.of("AcmeCorp"));
        var matches = detector.findMatches("deployed by acmecorp last night");
        assertThat(matches).extracting(DetectionMatch::value).containsExactly("acmecorp");
    }

    @Test
    void doesNotMatchInsideLargerWords() {
        var detector = new CustomKeywordDetector(Set.of("Apple"));
        assertThat(detector.findMatches("the Applegate orchard")).isEmpty();
    }

    @Test
    void treatsKeywordsAsLiteralsNotRegex() {
        // A '.' in a keyword must match a literal dot, not any character.
        var detector = new CustomKeywordDetector(Set.of("v1.0"));
        assertThat(detector.findMatches("shipped v1x0 today")).isEmpty();
        assertThat(detector.findMatches("shipped v1.0 today")).hasSize(1);
    }
}
