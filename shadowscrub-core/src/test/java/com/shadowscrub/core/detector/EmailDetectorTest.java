package com.shadowscrub.core.detector;

import com.shadowscrub.core.domain.PIIType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailDetectorTest {

    private final EmailDetector detector = new EmailDetector();

    @Test
    void detectsAddressWithReportedOffsets() {
        String input = "reach me at jane.doe+tag@sub.example.co.uk please";
        var matches = detector.findMatches(input);

        assertThat(matches).hasSize(1);
        DetectionMatch match = matches.get(0);
        assertThat(match.type()).isEqualTo(PIIType.EMAIL);
        assertThat(match.value()).isEqualTo("jane.doe+tag@sub.example.co.uk");
        assertThat(input.substring(match.start(), match.end())).isEqualTo(match.value());
    }

    @Test
    void detectsMultipleAddressesOnOneLine() {
        var matches = detector.findMatches("a@x.io, b@y.org");
        assertThat(matches).extracting(DetectionMatch::value)
            .containsExactly("a@x.io", "b@y.org");
    }

    @Test
    void ignoresTextThatIsNotAnEmail() {
        assertThat(detector.findMatches("no at-sign here, just @ and words")).isEmpty();
    }
}
