package com.shadowscrub.core.strategy;

import com.shadowscrub.core.detector.DetectionMatch;

/**
 * Replaces each match with a bracketed label of its type, e.g. {@code [EMAIL]}.
 * The original value is discarded entirely, so this is a one-way redaction with
 * no way to correlate two occurrences of the same value.
 */
public class MaskingStrategy implements ScrubbingStrategy {

    @Override
    public String scrub(DetectionMatch match) {
        return "[" + match.type().name() + "]";
    }
}
