package com.shadowscrub.core.engine;

import com.shadowscrub.core.detector.PIIDetector;
import com.shadowscrub.core.strategy.ScrubbingStrategy;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class ScrubbingEngine {
    private final List<PIIDetector> detectors;
    private final ScrubbingStrategy strategy;

    public ScrubbingEngine(List<PIIDetector> detectors, ScrubbingStrategy strategy) {
        this.detectors = detectors;
        this.strategy = strategy;
    }

    /**
     * Processes a Stream of lines asynchronously.
     * This allows us to handle files of any size without OutOfMemoryErrors.
     */
    public Stream<String> scrubStream(Stream<String> lines) {
        // Use a Virtual Thread Executor for high-concurrency line processing
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            
            return lines.map(line -> CompletableFuture.supplyAsync(() -> processLine(line), executor))
                        .map(CompletableFuture::join);
        }
    }

    private String processLine(String line) {
        if (line == null || line.isBlank()) return line;
        
        String processedLine = line;
        for (var detector : detectors) {
            var matches = detector.findMatches(processedLine);
            for (var match : matches) {
                processedLine = processedLine.replace(match.value(), strategy.scrub(match.value()));
            }
        }
        return processedLine;
    }
}