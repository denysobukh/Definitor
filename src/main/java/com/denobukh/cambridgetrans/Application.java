package com.denobukh.cambridgetrans;

import org.apache.commons.cli.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Words definition look up application
 */
public class Application {
    private static final String BASIC_URL = "https://dictionary.cambridge.org/dictionary";

    public static void main(String[] args) {
        Option inOpt = Option.builder("i").required().hasArg().desc("input file").build();
        Option sortOpt =
                Option.builder("s").longOpt("sort").desc("sort the words in the alphabetical order").build();
        Option mdOpt =
                Option.builder("md").longOpt("multiple-definitions").desc("load multiple definitions for each word").build();
        Option meOpt =
                Option.builder("me").longOpt("multiple-examples").desc("load examples for each definition").build();

        Options options = new Options();
        options
                .addOption(inOpt)
                .addOption(sortOpt)
                .addOption(mdOpt)
                .addOption(meOpt);

        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);
            new Application().run(cmd);
        } catch (MissingOptionException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("definitor -i <file> [-o <file>] [-md] [-me] [-s]", options);
            System.exit(-1);
        } catch (IOException | ParseException e) {
            System.out.println(e.getLocalizedMessage());
            System.exit(-1);
        }

    }

    /**
     * Loads words from the file
     * eache line in the file is considered as single word or phrase
     *
     * @param name of file
     * @return List of lines in the file
     * @throws FileNotFoundException if file can not be found
     */
    private List<String> loadFile(String name) throws FileNotFoundException {
        File f = new File(name);
        if (!f.exists()) throw new FileNotFoundException("File not found: " + name);

        List<String> lines = new LinkedList<>();
        try (Scanner scanner = new Scanner(f)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().toLowerCase().trim();
                if (!line.equals("") && !lines.contains(line))
                    lines.add(line);
            }
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        }

        return lines;
    }

    /**
     * Basic worker method:
     * loads lines from input file,
     * loads their definitions from the service,
     * writes them to the console or a file,
     * displays the progress.
     *
     * @param cmd arguments wich were specified during call from the command line
     * @throws IOException is thrown if input file can not be loaded,
     * definition can not be accessed,
     * output file can not be written
     */
    private void run(CommandLine cmd) throws IOException {
        StringBuilder outputBuilder = new StringBuilder();
        List<String> lines = loadFile(cmd.getOptionValue("i"));

        if (cmd.hasOption("s")) {
            Collections.sort(lines);
        }

        int foundCount = 0;
        int totalCount = lines.size();
        int missCount = 0;

        List<String> missWords = new LinkedList<>();

        for (String line : lines) {
            if (cmd.hasOption("o")) progress(foundCount, missCount, totalCount, line);

            WordDefinition definition = new WordDefinition(line).load(cmd.hasOption("md"), cmd.hasOption("me"));

            if (definition == null) {
                missWords.add(line);
                missCount++;
            } else {
                foundCount++;
            }

            outputBuilder.append(definition);

            if (cmd.hasOption("o")) System.out.print(progress(foundCount, missCount, totalCount, line));
        }
        if (cmd.hasOption("o")) System.out.print(progress(foundCount, missCount, totalCount));

        System.out.print(outputBuilder);

        System.out.println();
        if (missWords.size() > 0) {
            System.out.println("Not found: " + String.join(", ", missWords));
        }
    }

    /**
     * Composes status line string to depict the progress
     *
     * @param translated words number
     * @param notfound words number which were not found
     * @param total words number
     * @param word current word
     * @return status line
     */
    private String progress(int translated, int notfound, int total, String... word) {
        if (word.length == 0) {
            return String.format("\rTranslated: %-2d Missed: %-2d Total: %-2d                                ",
                    translated, notfound, total);
        } else {
            return String.format("\rTranslated: %-2d Missed: %-2d Total: %-2d [%s]                           ",
                    translated, notfound, total, word[0]);
        }
    }


    private static class WordDefinition {

        final String word;
        private WordMeanings wordMeanings = new WordMeanings();
        private List<String> pronunciationURLs = new LinkedList<>();

        private WordDefinition(String word) {
            this.word = word;
        }

        public List<String> getPronunciationURLs() {
            return pronunciationURLs;
        }

        /**
         *
         * @param multipleDefinitions if {@code true} multiple definitions is loaded, if {@code false} a first one only
         * @param multipleExamples if {@code true} multiple examples is loaded, if {@code false} a first one only
         * @return {@code WordDefinition} with result or
         * @throws IOException is thrown when connection error
         */
        WordDefinition load(boolean multipleDefinitions, boolean multipleExamples) throws IOException {

            String url = BASIC_URL + "/english/" + URLEncoder.encode(word, StandardCharsets.UTF_8);
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer(BASIC_URL)
                    .get();


            Elements senseBodyHTML = doc.select("div.dictionary:first-of-type div.sense-body");
            if (senseBodyHTML.size() > 0) {

                Elements definitionHTML;
                Element parent = null;


                /*
                if the multiple words are found in the line
                treats it as a phrase
                looks for the phrase match
                traverses through the parents elements forward the root
                until the definition block is found
                */
                boolean isPhrase = word.contains(" ");
                if (isPhrase) {
                    Elements phraseBlocks = doc.getElementsContainingOwnText(word);
                    for (Element e :
                            phraseBlocks) {
                        if(e.text().equals(word)) {
                            do {
                                e = e.parent();
                            } while (!e.hasClass("phrase-block") || doc.children().first().equals(e));
                            parent = e;
                            break;
                        }
                    }
                }
                if (parent == null) {
                    definitionHTML = senseBodyHTML.select("div.def-block");
                } else {
                    definitionHTML = new Elements();
                    definitionHTML.add(parent);
                }


                // extract definitions and usage examples into WordDefinition
                for (Element defElement : definitionHTML) {
                    String meaning = defElement.select("p.def-head b.def").text().trim();
                    if (meaning.endsWith(":")) meaning = meaning.substring(0, meaning.length() - 1);

                    wordMeanings.append(meaning);

                    Elements examplesHTML = defElement.select("div.examp");
                    for (Element ee : examplesHTML) {
                        String example = ee.text().trim();
                        if (!example.equals("")) {
                            wordMeanings.append(meaning, example);
                        }
                        if (!multipleExamples) break;
                    }

                    if (!multipleDefinitions) break;
                }

                // extract relative links to audio files containing the pronunciation
                Elements soundElements = doc.select("span.us span.sound");
                for (Element e :
                        soundElements) {
                    pronunciationURLs.add(BASIC_URL + e.attr("data-src-mp3"));
                    pronunciationURLs.add(BASIC_URL + e.attr("data-src-ogg"));
                }

            }
            return this;
        }


        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            Iterator<String> iterator = wordMeanings.getAll().keySet().iterator();
            String firstMeaning = iterator.hasNext() ? iterator.next() : null;

            wordMeanings.getAll().entrySet().forEach(e -> {
                String meaning = e.getKey();
                List<String> examples = e.getValue();

                if (meaning.equals(firstMeaning)) {
                    sb.append(String.format("%-12s ● %s ", word, meaning));
                } else {
                    sb.append(String.format("%-12s ● %s ", "", meaning));
                }

                if (examples.size() > 0)
                    sb.append(String.format("(Usage: %s)", String.join("  ", examples)));
                sb.append(String.format("%n"));
            });
            sb.append(String.format("%n"));
            return sb.toString();
        }

        // represents multiple word's meanings mapped to list of the usage examples
        private static class WordMeanings {

            final Map<String, List<String>> meanings = new LinkedHashMap<>();

            WordMeanings() {
            }

            private void append(String meaning, String example) {
                if (meanings.containsKey(meaning)) {
                    meanings.get(meaning).add(example);
                } else {
                    List<String> examples = new LinkedList<>();
                    examples.add(example);
                    meanings.put(meaning, examples);
                }
            }

            private void append(String meaning) {
                if (!meanings.containsKey(meaning)) {
                    meanings.put(meaning, new LinkedList<>());
                }
            }

            private Map<String, List<String>> getAll() {
                Map<String, List<String>> copy = new LinkedHashMap<>();
                for (Map.Entry<String, List<String>> entry :
                        meanings.entrySet()) {
                    copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
                }
                return copy;
            }
        }
    }

}