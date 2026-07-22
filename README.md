# 🛡️ ShadowScrub

A local-first command-line tool for anonymizing personally identifiable
information (PII). Point it at a file — a log, a CSV, a database dump — and it
redacts emails, credit-card numbers, IP addresses, and your own custom keywords
before the data leaves your machine for a cloud service, an LLM prompt, or an
analytics pipeline.

Everything happens locally. Nothing is uploaded.

```
$ shadowscrub app.log -o clean.log
$ diff app.log clean.log
< User bob@acme.io logged in from 10.0.0.1
> User [EMAIL] logged in from [IP_ADDRESS]
```

## Why

Sensitive data has a habit of ending up where it shouldn't — pasted into a chat
window, shipped to a third-party log aggregator, attached to a support ticket.
ShadowScrub is a small, fast pre-processing step you can run first. It is a
single binary with no services to stand up and no data leaving the host.

## What it detects

| Type | How it's found |
| :--- | :--- |
| **Email** | RFC 5322-style pattern covering the address shapes seen in practice |
| **Credit card** | Regex candidate match, then a **Luhn checksum** to drop serial numbers and IDs that merely look card-shaped |
| **IP address** | IPv4 with range validation (rejects `256.x`), plus full and compressed IPv6 |
| **Custom keywords** | Your own word list (`--keywords`), matched on word boundaries and treated as literals, not regex |

## How it works

- **Streaming.** Input is read line by line with `Files.lines`, so memory use is
  bounded by one batch of lines, not by file size. Multi-gigabyte files are fine.
- **Virtual threads.** Lines are scrubbed on Java 25 virtual threads in bounded
  batches. Throughput scales with available cores while output stays in input
  order.
- **Offset-based rewriting.** Each line is rebuilt from match offsets rather than
  string replacement, so a value that repeats on a line is handled correctly and
  an anonymized token is never re-scrubbed.
- **Pluggable strategies.** Detection is separate from what replaces a match. The
  current strategy is masking (`[EMAIL]`, `[CREDIT_CARD]`, …); more can be added
  behind the same interface (see the roadmap).

### Architecture

Two Gradle modules keep the engine independent of the CLI:

```
shadowscrub-core/     detection + scrubbing engine, no CLI dependencies
  detector/           PIIDetector implementations + DetectionMatch
  strategy/           ScrubbingStrategy (MaskingStrategy)
  engine/             ScrubbingEngine — streams lines through detectors
  domain/             PIIType

shadowscrub-cli/      Picocli front end, depends on core
```

## Requirements

- **JDK 25** (uses virtual threads and modern language features)
- The bundled Gradle wrapper handles the rest — no separate Gradle install needed

## Build

```bash
./gradlew build          # compile + run tests
./gradlew installDist    # produce a runnable distribution
```

The runnable launcher lands at
`shadowscrub-cli/build/install/shadowscrub-cli/bin/shadowscrub-cli`.

## Usage

```bash
# Redact a log file (defaults to scrubbed_output.txt)
shadowscrub app.log -o clean.log

# Add a custom keyword dictionary
shadowscrub app.log -k "AcmeCorp,ProjectApollo" -o clean.log
```

During development you can run it straight from Gradle without installing:

```bash
./gradlew :shadowscrub-cli:run --args="app.log -o clean.log -k AcmeCorp"
```

### Options

| Flag | Description |
| :--- | :--- |
| `<file>` | File to scrub (required) |
| `-o`, `--output` | Output path (default: `scrubbed_output.txt`) |
| `-k`, `--keywords` | Comma-separated custom keywords to redact |
| `-h`, `--help` | Show help |
| `-V`, `--version` | Show version |

### Example

Input:

```
User bob@acme.io logged in from 10.0.0.1
Payment 4242 4242 4242 4242 approved for jane@x.org
Serial 1234-5678-9012-3456 is not a card
Project AcmeCorp deployed to 2001:db8::1
```

Output (`-k AcmeCorp`):

```
User [EMAIL] logged in from [IP_ADDRESS]
Payment [CREDIT_CARD] approved for [EMAIL]
Serial 1234-5678-9012-3456 is not a card
Project [CUSTOM] deployed to [IP_ADDRESS]
```

Note the serial number is left untouched — it looks like a card but fails the
Luhn check.

## Development

```bash
./gradlew test           # run the test suite (JUnit 5 + AssertJ)
./gradlew :shadowscrub-core:test --tests "*ScrubbingEngineTest"
```

Detectors are stateless and safe to run concurrently; the engine relies on that
when it fans lines out across virtual threads.

## Roadmap

- [x] Streaming virtual-thread engine
- [x] Email, credit-card (Luhn), IP (v4/v6), and custom-keyword detection
- [x] Masking strategy
- [ ] **Salted SHA-256 hashing strategy** — consistent pseudonymous tokens that
      preserve cross-referencing (`-s/--strategy`, `--salt`)
- [ ] Per-run summary report (counts by PII type)
- [ ] GitHub Actions CI (build + test on JDK 25)
- [ ] Local web UI

## License

MIT — see [LICENSE](LICENSE).
