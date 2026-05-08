package com.denobukh.definitionfinder;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Interface for fetching HTML pages from the dictionary.
 * Allows injection of fixture-based implementations in tests.
 */
public interface PageFetcher {
    Document fetch(String url) throws IOException;
}

/**
 * Default implementation that performs live HTTP requests via Jsoup.
 */
class HttpPageFetcher implements PageFetcher {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6";
    private static final String REFERRER = "https://dictionary.cambridge.org/dictionary";

    @Override
    public Document fetch(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .referrer(REFERRER)
                .get();
    }
}
