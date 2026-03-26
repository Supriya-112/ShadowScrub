package com.shadowscrub.cli;

import com.shadowscrub.core.engine.ScrubbingEngine;
import com.shadowscrub.core.detector.EmailDetector;
import com.shadowscrub.core.strategy.MaskingStrategy;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.BufferedWriter;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

@Command(name = "shadowscrub", mixinStandardHelpOptions = true, version = "1.0",
        description = "ShadowScrub: Stream-based PII Anonymizer for Big Data.")
public class ShadowScrubCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Input file path.")
    private Path inputPath;

    @Option(names = {"-o", "--output"}, description = "Output file path (optional).", defaultValue = "scrubbed_output.txt")
    private Path outputPath;

    @Override
    public Integer call() throws Exception {
        if (!Files.exists(inputPath)) {
            System.err.println("❌ Error: Input file not found.");
            return 1;
        }

        System.out.println("🛡️  ShadowScrub is streaming: " + inputPath.getFileName());

        var engine = new ScrubbingEngine(List.of(new EmailDetector()), new MaskingStrategy());

        try (Stream<String> lines = Files.lines(inputPath);
             BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            
            engine.scrubStream(lines).forEach(line -> {
                try {
                    writer.write(line);
                    writer.newLine();
                } catch (Exception e) {
                    throw new RuntimeException("Error writing to output file", e);
                }
            });
        }

        System.out.println("✅ Scrubbed data saved to: " + outputPath.toAbsolutePath());
        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ShadowScrubCommand()).execute(args);
        System.exit(exitCode);
    }
}