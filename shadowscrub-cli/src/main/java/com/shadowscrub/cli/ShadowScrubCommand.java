package com.shadowscrub.cli;

import com.shadowscrub.core.engine.ScrubbingEngine;
import com.shadowscrub.core.detector.*;
import com.shadowscrub.core.strategy.MaskingStrategy;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * The CLI entry point for ShadowScrub.
 * Uses Picocli for command-line parsing and Java NIO for memory-efficient streaming.
 */
@Command(
    name = "shadowscrub", 
    mixinStandardHelpOptions = true, 
    version = "1.0",
    description = "🛡️  ShadowScrub: High-performance, local-first PII Anonymizer."
)
public class ShadowScrubCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "The path to the input file you want to scrub.")
    private Path inputPath;

    @Option(names = {"-o", "--output"}, 
            description = "The path for the scrubbed output file.", 
            defaultValue = "scrubbed_output.txt")
    private Path outputPath;

    @Option(names = {"-k", "--keywords"}, 
            split = ",", 
            description = "Optional: Custom keywords to scrub (e.g., 'CompanyInc,ProjectX').")
    private Set<String> customKeywords;

    @Override
    public Integer call() throws Exception {
        if (!Files.exists(inputPath)) {
            System.err.println("❌ Error: Input file not found at " + inputPath.toAbsolutePath());
            return 1;
        }

        if (Files.isDirectory(inputPath)) {
            System.err.println("❌ Error: Input path is a directory. Please provide a file.");
            return 1;
        }

        System.out.println("\n🚀 Initializing ShadowScrub Engine...");
        System.out.println("📂 Input:  " + inputPath.getFileName());
        System.out.println("📍 Output: " + outputPath.toAbsolutePath());

        List<PIIDetector> detectors = new ArrayList<>();
        detectors.add(new EmailDetector());
        detectors.add(new CreditCardDetector()); 
        detectors.add(new IPAddressDetector());   

        // Add user-defined keywords if provided
        if (customKeywords != null && !customKeywords.isEmpty()) {
            detectors.add(new CustomKeywordDetector(customKeywords));
            System.out.println("🏷️  Custom Dictionary Loaded: " + customKeywords);
        }

        // Initialize the Engine with a Masking Strategy
        var engine = new ScrubbingEngine(detectors, new MaskingStrategy());

        try (Stream<String> lines = Files.lines(inputPath);
             BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            
            System.out.println("⚙️  Processing via Java 25 Virtual Threads...");
            
            engine.scrubStream(lines).forEach(line -> {
                try {
                    writer.write(line);
                    writer.newLine();
                } catch (Exception e) {
                    throw new RuntimeException("CRITICAL: Failed to write to output file", e);
                }
            });
        } catch (Exception e) {
            System.err.println("❌ Critical Engine Error: " + e.getMessage());
            return 1;
        }

        System.out.println("\n✅ Success! All detected PII has been anonymized.");
        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ShadowScrubCommand()).execute(args);
        System.exit(exitCode);
    }
}