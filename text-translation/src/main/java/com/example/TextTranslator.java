package com.example;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

public class TextTranslator {
    public static void main(String[] args) {
        Translate translate = TranslateOptions.getDefaultInstance().getService();

        String text = "Hello, how are you?";
        Translation translation = translate.translate(text, Translate.TranslateOption.targetLanguage("hi"));

        System.out.println("English: " + text);
        System.out.println("Hindi: " + translation.getTranslatedText());
    }
}
