package com.shadowscrub.core.detector;

import com.shadowscrub.core.domain.PIIType;

/**
 * A single piece of PII located within a line of text.
 *
 * @param start inclusive start offset within the source line
 * @param end   exclusive end offset within the source line
 * @param value the matched text
 * @param type  the kind of PII this match represents
 */
public record DetectionMatch(int start, int end, String value, PIIType type) {
}
