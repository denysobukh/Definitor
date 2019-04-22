package com.denobukh.definitionfinder.test;

import com.denobukh.definitionfinder.Application;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * ApplicationTest class
 *
 * @author Dennis Obukhov
 * @date 2019-04-22 13:03 [Monday]
 */
public class ApplicationTest {

    private ByteArrayOutputStream byteArrayOutputStream;
    private PrintStream oldOut;

    @BeforeEach
    public void init() {
        System.out.println("Working dir: " + System.getProperty("user.dir"));
    }

    @AfterEach
    void restoreOutput() {
        System.setOut(oldOut);
    }

    private String getOutputString() {
        // Put things back
        System.out.flush();
        return byteArrayOutputStream.toString();
    }

    private String readFile(String fileName) {
        String output = null;
        try {
            output = new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (IOException e) {
            fail(e);
        }
        return output;
    }

    private void interceptOutput() {
        /* Create a stream to hold the output */
        byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        // IMPORTANT: Save the old System.out!
        oldOut = System.out;
        // Tell Java to use your special stream
        System.setOut(printStream);
    }

    @Test
    public void mainTest_bookWords_success() {
        String expected = readFile("src/test/resources/bookwords_expected.txt");
        interceptOutput();
        Application.main(new String[]{"-i", "src/test/resources/bookwords.txt"});
        restoreOutput();
        assertEquals(expected, getOutputString());
    }
}
