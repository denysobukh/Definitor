package com.denobukh.definitionfinder.test;

import com.denobukh.definitionfinder.Application;
import com.denobukh.definitionfinder.PageFetcher;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * ApplicationTest class — uses HTML fixtures instead of live HTTP calls.
 */
public class ApplicationTest {

    private ByteArrayOutputStream byteArrayOutputStream;
    private PrintStream oldOut;

    @BeforeEach
    public void init() {
        byteArrayOutputStream = new ByteArrayOutputStream();
        oldOut = System.out;
        System.setOut(new PrintStream(byteArrayOutputStream));
    }

    @AfterEach
    void restoreOutput() {
        System.setOut(oldOut);
    }

    private String getOutputString() {
        System.out.flush();
        return byteArrayOutputStream.toString();
    }

    /**
     * A PageFetcher that loads HTML from fixture files instead of making HTTP requests.
     * Maps dictionary URLs to fixture files in src/test/fixtures/.
     * URL-encoded words (e.g. "hello") map to hello.html.
     * URL-encoded phrases (e.g. "hold+on+to") map to hold_on_to.html.
     */
    private static class FixturePageFetcher implements PageFetcher {
        private static final String FIXTURE_PATH = "src/test/fixtures/";

        @Override
        public Document fetch(String url) throws IOException {
            // Extract the encoded word from URL like "https://dictionary.cambridge.org/dictionary/english/hello"
            String encodedWord = url.substring(url.lastIndexOf('/') + 1);
            String decodedWord = URLDecoder.decode(encodedWord, StandardCharsets.UTF_8.name());
            // Replace spaces with underscores for fixture filenames: "hold on to" -> "hold_on_to"
            String fixtureName = decodedWord.replace(" ", "_") + ".html";
            String fixtureFile = FIXTURE_PATH + fixtureName;
            String html = new String(Files.readAllBytes(Paths.get(fixtureFile)), "UTF-8");
            return Jsoup.parse(html, "UTF-8");
        }
    }

    @Test
    public void mainTest_fixtures_success() throws IOException, ParseException {
        String expected = readFile("src/test/resources/expected_output.txt");

        Options options = new Options();
        options.addOption("i", true, "input file");
        options.addOption("s", false, "sort");
        options.addOption("md", false, "multiple definitions");
        options.addOption("me", false, "multiple examples");

        Application app = new Application();
        CommandLine cmd = new DefaultParser().parse(options, new String[]{"-i", "src/test/resources/test_words.txt", "-md", "-me"});

        app.run(cmd, new FixturePageFetcher());

        assertEquals(expected, getOutputString());
    }

    private String readFile(String fileName) {
        try {
            return new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (IOException e) {
            fail(e);
        }
        return null;
    }
}
