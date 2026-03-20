package com.shadowscrub.cli;

import com.shadowscrub.core.engine.ScrubbingEngine;
import com.shadowscrub.core.detector.EmailDetector;
import com.shadowscrub.core.strategy.MaskingStrategy;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import java.nio.file.Files;
import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "shadowscrub", mixinStandardHelpOptions = true, version = "1.0",
        description = "ShadowScrub: Local-first PII Anonymizer.")
public class ShadowScrubCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "The path to the file you want to scrub.")
    private File file;

    @Override
    public Integer call() throws Exception {
        if (!file.exists()) {
            System.err.println("❌ Error: File not found at " + file.getAbsolutePath());
            return 1;
        }

        System.out.println("🛡️  ShadowScrub is scanning: " + file.getName());

        // Senior Design: Initialize engine with our detectors
        var engine = new ScrubbingEngine(List.of(new EmailDetector()), new MaskingStrategy());
        
        // Read the file, scrub it, and print to console (or save back)
        String content = Files.readString(file.toPath());
        String scrubbed = engine.scrubText(content);

        System.out.println("\n--- SCRUBBED OUTPUT ---");
        System.out.println(scrubbed);
        System.out.println("-----------------------");
        System.out.println("✅ Processing complete.");
        
        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ShadowScrubCommand()).execute(args);
        System.exit(exitCode);
    }
}