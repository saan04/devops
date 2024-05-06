package com.example;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SpeechToTextTranslator {
    public static void main(String[] args) {
        String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        System.out.println("Using credentials from: " + credentialsPath);

        if (credentialsPath == null || credentialsPath.isEmpty()) {
            System.err.println("ERROR: GOOGLE_APPLICATION_CREDENTIALS environment variable is not set.");
            return;
        }
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(Files.newInputStream(Paths.get(credentialsPath)));
            SpeechClient speechClient = SpeechClient.create();

            Path audioFilePath = Paths.get("/Users/saanvinair/IdeaProjects/gradle-translation/speech-to-text/test_sample_audio.wav");
            byte[] audioData = Files.readAllBytes(audioFilePath);
            ByteString audioBytes = ByteString.copyFrom(audioData);

            RecognitionConfig config =
                    RecognitionConfig.newBuilder()
                            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                            .setLanguageCode("en-US")
                            .setSampleRateHertz(16000)
                            .build();

            RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(audioBytes).build();

            RecognizeResponse response = speechClient.recognize(config, audio);
            for (SpeechRecognitionResult result : response.getResultsList()) {
                for (SpeechRecognitionAlternative alternative : result.getAlternativesList()) {
                    System.out.println("Transcript: " + alternative.getTranscript());
                }
            }

            speechClient.close();
        } catch (IOException e) {
            System.err.println("Error reading credentials file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error during speech recognition: " + e.getMessage());
        }
    }
}
