package com.shadowscrub.core.detector;

import com.shadowscrub.core.domain.PIIType;
import java.util.List;

/**
 * Locates occurrences of one kind of PII in a line of text.
 *
 * <p>Detectors are stateless and must be safe to call from multiple threads,
 * since the engine runs many lines concurrently against a shared detector set.
 */
public interface PIIDetector {

    /** The kind of PII this detector recognises. */
    PIIType getType();

    /**
     * Finds every match in {@code input}. Offsets in the returned matches are
     * relative to {@code input} and may overlap matches from other detectors;
     * resolving overlaps is the engine's job, not the detector's.
     */
    List<DetectionMatch> findMatches(String input);
}
