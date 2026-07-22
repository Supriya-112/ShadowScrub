package com.shadowscrub.cli;

import com.shadowscrub.core.detector.CreditCardDetector;
import com.shadowscrub.core.detector.CustomKeywordDetector;
import com.shadowscrub.core.detector.EmailDetector;
import com.shadowscrub.core.detector.IPAddressDetector;
import com.shadowscrub.core.detector.PIIDetector;
import com.shadowscrub.core.engine.ScrubbingEngine;
import com.shadowscrub.core.strategy.MaskingStrategy;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * Command-line entry point. Reads the input file as a stream, runs it through
 * the scrubbing engine, and writes the anonymised result to the output file.
 */
@Command(
    name = "shadowscrub",
    mixinStandardHelpOptions = true,
    version = "ShadowScrub 1.0",
    description = "Local-first PII anonymizer and data sanitizer."
)
public class ShadowScrubCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "File to scrub.")
    private Path inputPath;

    @Option(names = {"-o", "--output"},
            description = "Where to write the scrubbed output (default: ${DEFAULT-VALUE}).",
            defaultValue = "scrubbed_output.txt")
    private Path outputPath;

    @Option(names = {"-k", "--keywords"},
            split = ",",
            description = "Extra keywords to redact, e.g. 'AcmeCorp,ProjectApollo'.")
    private Set<String> customKeywords;

    private final PrintStream out = System.out;
    private final PrintStream err = System.err;

    @Override
    public Integer call() throws IOException {
        if (!Files.exists(inputPath)) {
            err.println("Input file not found: " + inputPath.toAbsolutePath());
            return 1;
        }
        if (Files.isDirectory(inputPath)) {
            err.println("Input path is a directory; expected a file: " + inputPath);
            return 1;
        }

        var engine = new ScrubbingEngine(buildDetectors(), new MaskingStrategy());

        try (Stream<String> lines = Files.lines(inputPath);
             BufferedWriter writer = Files.newBufferedWriter(outputPath)) {

            engine.scrub(lines, line -> writeLine(writer, line));
        } catch (UncheckedIOException e) {
            err.println("Failed writing output: " + e.getCause().getMessage());
            return 1;
        }

        out.println("Done. Scrubbed output written to " + outputPath.toAbsolutePath());
        return 0;
    }

    private List<PIIDetector> buildDetectors() {
        List<PIIDetector> detectors = new ArrayList<>();
        detectors.add(new EmailDetector());
        detectors.add(new CreditCardDetector());
        detectors.add(new IPAddressDetector());
        if (customKeywords != null && !customKeywords.isEmpty()) {
            detectors.add(new CustomKeywordDetector(customKeywords));
        }
        return detectors;
    }

    private static void writeLine(BufferedWriter writer, String line) {
        try {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            // Surfaced as a clean message by call(); unchecked so it can escape
            // the Consumer the engine calls us through.
            throw new UncheckedIOException(e);
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ShadowScrubCommand()).execute(args);
        System.exit(exitCode);
    }
}
