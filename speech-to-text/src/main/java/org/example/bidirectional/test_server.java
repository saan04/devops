package org.example.bidirectional;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.example.gradletranslation.GreeterGrpc;
import org.example.gradletranslation.audioRequest;
import org.example.gradletranslation.audioResponse;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;

//import org.example.translation.getTranslation;

public class test_server {

    private final Configuration configuration;
    private final StreamSpeechRecognizer recognizer;

    private test_server(String acousticModelPath, String dictionaryPath, String languageModelPath) throws IOException {
        configuration = new Configuration();

        configuration.setAcousticModelPath(acousticModelPath);
        configuration.setDictionaryPath(dictionaryPath);
        configuration.setLanguageModelPath(languageModelPath);

        recognizer = new StreamSpeechRecognizer(configuration);

    }

    private void start() throws IOException {
        int port = 9090;
        Server server = ServerBuilder.forPort(port)
                .addService(new GreeterImpl())
                .build();
        server.start();
        System.out.println("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        try {
            server.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }

    private class GreeterImpl extends GreeterGrpc.GreeterImplBase {
        @Override
        public void sendRequest(audioRequest request, StreamObserver<audioResponse> responseObserver) {
            List<Integer> audioSamples = request.getAudioByteList();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //getTranslation translation = new getTranslation();
            for (int num : audioSamples) {
                baos.write((byte) (num & 0xFF));
                baos.write((byte) ((num >> 8) & 0xFF));
            }
            ByteArrayInputStream inputAudio =  new ByteArrayInputStream(baos.toByteArray());

            System.out.println("Got a request!");
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> {
                try {
                    // Create a new recognizer for each request to prevent resource leaks
                    StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(configuration);
                    // Initialize recognizer
                    recognizer.startRecognition(inputAudio);

                    SpeechResult result;
                    while ((result = recognizer.getResult()) != null) {
                        System.out.format("Hypothesis: %s\n", result.getHypothesis());
                        //String translatedText = translation.Translate(result.getHypothesis());
                        audioResponse response = audioResponse.newBuilder()
                                .setTranscript(result.getHypothesis())
                                .build();
                        responseObserver.onNext(response);
                    }
                    recognizer.stopRecognition();
                    responseObserver.onCompleted();

                    recognizer.stopRecognition();
                } catch (Exception e) {
                    e.printStackTrace();
                    responseObserver.onError(e);
                }
            });
        }
    }

    public static void main(String[] args) throws IOException {
        String acousticModelPath = "resource:/edu/cmu/sphinx/models/en-us/en-us";
        String dictionaryPath = "resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict";
        String languageModelPath = "resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin";

        test_server server = new test_server(acousticModelPath, dictionaryPath, languageModelPath);
        server.start();
    }
}
