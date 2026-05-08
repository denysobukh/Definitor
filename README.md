# Definition Finder

> A command-line tool for looking up English word definitions from the **Cambridge Dictionary**.

Feed it a list of words, get back clean, formatted definitions with usage examples.


## Features

- **Batch lookup** — process hundreds of words from a single input file
- **Phrase support** — multi-word phrases are resolved correctly
- **Deduplication** — duplicate entries are automatically removed (case-insensitive)
- **Multiple definitions** — fetch all meanings, not just the first
- **Usage examples** — include example sentences for each definition
- **Sorted output** — optional alphabetical sorting of results


## Requirements

- **Java 10+** (compiled with source/target level 10)
- **Maven 3.x** (for building from source)
- Internet connection (live lookups via Cambridge Dictionary)


## Installation

### Build from source

```bash
mvn clean package
```

The resulting JAR will be at `target/definitionfinder-1.0-SNAPSHOT.jar`.

### Quick start

```bash
java -jar target/definitionfinder-1.0-SNAPSHOT.jar -i words.txt
```


## Usage

```
java -jar definitionfinder-1.0-SNAPSHOT.jar -i <file> [-s] [-md] [-me]
```

### Options

| Flag | Description |
|---|---|
| `-i <file>` | **(Required)** Input file containing words, one per line |
| `-s`, `--sort` | Sort words alphabetically before processing |
| `-md`, `--multiple-definitions` | Fetch all definitions for each word (not just the first) |
| `-me`, `--multiple-examples` | Fetch all usage examples for each definition |

### Input file format

One word or phrase per line, plain text:

```
ephemeral
hold on to
ubiquitous
serendipity
```

### Example

```bash
# Basic lookup
java -jar definitionfinder-1.0-SNAPSHOT.jar -i words.txt

# All definitions with examples, sorted
java -jar definitionfinder-1.0-SNAPSHOT.jar -i words.txt -md -me -s
```

### Output format

Results are printed to **stdout** in a fixed-width, column-aligned format:

```
ephemeral    ● lasting for a very short time (Usage: Ephemeral pleasures.)
ubiquitous   ● seeming to be found everywhere (Usage: Smartphones are now ubiquitous.)
```

Words that could not be looked up are reported at the end.


## Architecture

```
Application.java
├── main()            — CLI entry point, orchestrates the workflow
├── WordDefinition    — encapsulates a word + its scraped definition data
│   ├── load()        — HTTP request → HTML parse → extract definitions/examples
│   └── toString()    — formats output as aligned columns
└── WordMeanings      — maps each definition meaning → list of usage examples
```

### Dependencies

| Dependency | Purpose |
|---|---|
| [Jsoup](https://jsoup.org/) 1.11.3 | HTML parsing and HTTP requests |
| [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/) 1.4 | Command-line argument parsing |
| [JUnit 5](https://junit.org/junit5/) 5.4.2 | Testing (test scope) |


## Testing

```bash
mvn test
```

Test resources are located under `src/test/resources/` and include several word-list fixtures.


## Limitations

- **No rate limiting** — large input files may trigger rate-limiting from Cambridge Dictionary
- **Live scraping only** — definitions are fetched in real-time; there is no offline cache
- **Single-threaded** — words are processed sequentially


## License

This project is provided as-is. No formal license has been specified.
