package com.shadowscrub.core.engine;

import com.shadowscrub.core.detector.EmailDetector;
import com.shadowscrub.core.detector.IPAddressDetector;
import com.shadowscrub.core.strategy.MaskingStrategy;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ScrubbingEngineTest {

    private final ScrubbingEngine engine = new ScrubbingEngine(
        List.of(new EmailDetector(), new IPAddressDetector()),
        new MaskingStrategy()
    );

    @Test
    void masksDetectedPiiAndLeavesTheRestUntouched() {
        List<String> out = scrub("contact bob@acme.io from 10.0.0.1 now");
        assertThat(out).containsExactly("contact [EMAIL] from [IP_ADDRESS] now");
    }

    @Test
    void masksEveryOccurrenceOfARepeatedValue() {
        // Guards against the old substring-replace bug: a value repeated on the
        // same line must be masked at both offsets, not once.
        List<String> out = scrub("a@x.io then a@x.io");
        assertThat(out).containsExactly("[EMAIL] then [EMAIL]");
    }

    @Test
    void leavesLinesWithoutPiiExactlyAsIs() {
        List<String> out = scrub("nothing sensitive here", "", "   ");
        assertThat(out).containsExactly("nothing sensitive here", "", "   ");
    }

    @Test
    void preservesInputOrderAcrossManyLines() {
        String[] input = IntStream.range(0, 5000)
            .mapToObj(i -> "line " + i + " user" + i + "@acme.io")
            .toArray(String[]::new);

        List<String> out = scrub(input);

        assertThat(out).hasSize(5000);
        for (int i = 0; i < input.length; i++) {
            assertThat(out.get(i)).isEqualTo("line " + i + " [EMAIL]");
        }
    }

    private List<String> scrub(String... lines) {
        List<String> collected = new ArrayList<>();
        try (Stream<String> stream = Stream.of(lines)) {
            engine.scrub(stream, collected::add);
        }
        return collected;
    }
}
