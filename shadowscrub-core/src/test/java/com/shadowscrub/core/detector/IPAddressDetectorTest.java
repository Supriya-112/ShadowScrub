package com.shadowscrub.core.detector;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IPAddressDetectorTest {

    private final IPAddressDetector detector = new IPAddressDetector();

    @Test
    void detectsIPv4() {
        var matches = detector.findMatches("client 192.168.1.254 connected");
        assertThat(matches).extracting(DetectionMatch::value).containsExactly("192.168.1.254");
    }

    @Test
    void rejectsOutOfRangeIPv4() {
        assertThat(detector.findMatches("version 256.300.1.1 build")).isEmpty();
    }

    @Test
    void detectsFullIPv6() {
        var matches = detector.findMatches("host 2001:0db8:85a3:0000:0000:8a2e:0370:7334 up");
        assertThat(matches).extracting(DetectionMatch::value)
            .containsExactly("2001:0db8:85a3:0000:0000:8a2e:0370:7334");
    }

    @Test
    void detectsCompressedIPv6() {
        var matches = detector.findMatches("gateway 2001:db8::1 ready");
        assertThat(matches).extracting(DetectionMatch::value).contains("2001:db8::1");
    }
}
