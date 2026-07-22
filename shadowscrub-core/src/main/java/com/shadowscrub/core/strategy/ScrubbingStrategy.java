package com.shadowscrub.core.strategy;

import com.shadowscrub.core.detector.DetectionMatch;

/**
 * Turns a detected piece of PII into its anonymised replacement.
 *
 * <p>The whole match is passed (not just its text) so a strategy can vary its
 * output by PII type, e.g. emitting {@code [EMAIL]} versus {@code [CREDIT_CARD]}.
 * Implementations must be thread-safe; one instance handles the whole file.
 */
public interface ScrubbingStrategy {

    /** Returns the text that should replace {@code match} in the output. */
    String scrub(DetectionMatch match);
}
