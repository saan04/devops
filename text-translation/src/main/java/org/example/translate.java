package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class translate {

    public String translate(String text) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://translate-language.p.rapidapi.com/translate?to_language=Hindi&from_language=English"))
                .header("content-type", "application/json")
                .header("X-RapidAPI-Key", "868b89647cmshb728feb99d2597fp11186fjsn77edaf12fb3d")
                .header("X-RapidAPI-Host", "translate-language.p.rapidapi.com")
                .method("POST", HttpRequest.BodyPublishers.ofString("{\n    \"text\": \"" + text + "\"\n}"))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonObject data = jsonObject.getAsJsonObject("data");
        String unicodeText = data.get("text").getAsString();
        // Handle unicode conversion to characters if needed

        return unicodeText;
    }

    public static void main(String[] args) {
        translate translator = new translate();

        // Path to the input text file containing English text
        String filePath = "/Users/saanvinair/IdeaProjects/gradle-translation/text-translation/hello.txt";

        try {
            // Read the content of the text file
            String text = readFile(filePath);

            // Translate the text
            String translatedText = translator.translate(text);

            System.out.println("Translated Text: " + translatedText);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String readFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes);
    }
}
