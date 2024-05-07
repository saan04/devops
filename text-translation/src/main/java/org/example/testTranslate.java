package org.example;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class testTranslate {

    private translate translator;

    @Before
    public void setUp() {
        translator = new translate();
    }

    @Test
    public void testTranslate() throws Exception {
        // Input text to be translated
        String inputText = "Hello, world!";

        // Expected translated text (mock response for testing)
        String expectedTranslatedText = "नमस्ते, दुनिया!";

        // Call the translate method
        String translatedText = translator.translate(inputText);

        // Assert that the translated text matches the expected result
        assertEquals(expectedTranslatedText, translatedText);
    }
}
