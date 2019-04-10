package com.denobukh.cambridgetrans;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;


public class Application {
    private static final String HOST = "https://dictionary.cambridge.org/dictionary";

    private static int translated, total, miss;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: cambridgetrans <in file> <out file>");
            System.exit(-1);
        }

        File inFile = new File(args[0]);
        if (!inFile.exists()) {
            System.out.println("File not found: " + args[0]);
            System.exit(-1);
        }

        Set<String> words = new TreeSet<>();
        try (Scanner scanner = new Scanner(inFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().toLowerCase().trim();
                if (!line.equals(""))
                    words.add(line);
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getLocalizedMessage());
            System.exit(-1);
        }

        total = words.size();

        if (total == 0) {
            System.out.println("0 lines to translate");
            System.exit(0);
        }

        Set<String> missWords = new TreeSet<>();
        try (FileWriter fileWriter = new FileWriter(new File(args[1]), false)) {
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            for (String word : words) {

                progress(word);

                String url = HOST + "/english/" + word.replace(' ', '-');
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("https://dictionary.cambridge.org/dictionary/")
                        .get();

                Elements senseBody = doc.select("div.sense-body");
                if (senseBody.size() > 0) {
                    Elements defBlocks = senseBody.select("div.def-block");
                    Element firstDef = defBlocks.first();

                    String defStr = firstDef.select("p.def-head b.def").text().trim();
                    if (defStr.endsWith(":")) defStr = defStr.substring(0, defStr.length() - 1);
                    bufferedWriter.write(String.format("%-12s - %s", word, defStr));

                    Element examp = firstDef.select("div.examp").first();
                    if (examp != null) {
                        String exampStr = examp.text().trim();
                        if (!exampStr.equals("")) {
                            bufferedWriter.newLine();
                            bufferedWriter.write("(" + exampStr + ")");
                        }
                    }

                    bufferedWriter.newLine();
                    bufferedWriter.newLine();

                    /*
                    Elements sounds = doc.select("span.us span.sound");
                    Element e = sounds.first();
                    System.out.println(HOST + e.attr("data-src-mp3"));
                    System.out.println(HOST + e.attr("data-src-ogg"));
                    */
                } else {
                    missWords.add(word);
                    miss = missWords.size();
                }

                translated++;
                System.out.print(progress(word));
            }
            System.out.print(progress());

        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
            System.exit(-1);
        }
        progress();
        System.out.println();
        if (missWords.size() > 0) {
            System.out.println("Not found: " + String.join(", ", missWords));
        }
    }

    private static String progress(String... word) {
        if (word.length == 0) {
            return String.format("\rTranslated: %-2d Missed: %-2d Total: %-2d                                ",
                    translated, miss, total);
        } else {
            return String.format("\rTranslated: %-2d Missed: %-2d Total: %-2d [%s]", translated, miss, total, word[0]);
        }
    }

}