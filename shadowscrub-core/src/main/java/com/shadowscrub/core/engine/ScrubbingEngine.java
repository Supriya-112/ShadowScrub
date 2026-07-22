package com.shadowscrub.core.engine;

import com.shadowscrub.core.detector.DetectionMatch;
import com.shadowscrub.core.detector.PIIDetector;
import com.shadowscrub.core.strategy.ScrubbingStrategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Applies a set of detectors and a scrubbing strategy to a stream of lines.
 *
 * <p>Lines are processed on virtual threads in bounded batches: at most
 * {@link #BATCH_SIZE} lines are in flight at once, so memory stays proportional
 * to the batch rather than the file. Results are emitted in input order.
 */
public class ScrubbingEngine {

    // Enough concurrent work to keep detection off the I/O critical path,
    // while capping how many lines (and their futures) are held in memory.
    private static final int BATCH_SIZE = 1024;

    private final List<PIIDetector> detectors;
    private final ScrubbingStrategy strategy;

    public ScrubbingEngine(List<PIIDetector> detectors, ScrubbingStrategy strategy) {
        this.detectors = List.copyOf(detectors);
        this.strategy = strategy;
    }

    /**
     * Scrubs every line from {@code lines} and hands the result to {@code sink}
     * in the original order. The stream is consumed lazily one batch at a time;
     * the caller owns closing it.
     */
    public void scrub(Stream<String> lines, Consumer<String> sink) {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<String>> batch = new ArrayList<>(BATCH_SIZE);
            var iterator = lines.iterator();

            while (iterator.hasNext()) {
                batch.clear();
                while (batch.size() < BATCH_SIZE && iterator.hasNext()) {
                    String line = iterator.next();
                    batch.add(executor.submit(() -> processLine(line)));
                }
                for (Future<String> future : batch) {
                    sink.accept(await(future));
                }
            }
        }
    }

    private static String await(Future<String> future) {
        try {
            return future.get();
        } catch (ExecutionException e) {
            throw new RuntimeException("scrubbing a line failed", e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("interrupted while scrubbing", e);
        }
    }

    /**
     * Rewrites a single line by walking its matches left to right and splicing
     * in each replacement. Working from offsets (rather than substring replace)
     * keeps the rewrite exact even when a value repeats or an anonymised token
     * happens to look like another match.
     */
    private String processLine(String line) {
        if (line == null || line.isEmpty()) {
            return line;
        }

        List<DetectionMatch> matches = new ArrayList<>();
        for (PIIDetector detector : detectors) {
            matches.addAll(detector.findMatches(line));
        }
        if (matches.isEmpty()) {
            return line;
        }
        matches.sort(Comparator.comparingInt(DetectionMatch::start));

        StringBuilder out = new StringBuilder(line.length());
        int cursor = 0;
        for (DetectionMatch match : matches) {
            // Skip matches that start inside a span already scrubbed; the first
            // detector to claim a region wins.
            if (match.start() < cursor) {
                continue;
            }
            out.append(line, cursor, match.start());
            out.append(strategy.scrub(match));
            cursor = match.end();
        }
        out.append(line, cursor, line.length());
        return out.toString();
    }
}
