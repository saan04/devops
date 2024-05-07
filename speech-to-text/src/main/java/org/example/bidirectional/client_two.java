//package org.example.bidirectional;
//import io.grpc.stub.StreamObserver;
//import io.grpc.ManagedChannel;
//import io.grpc.ManagedChannelBuilder;
//import org.example.gradletranslation.GreeterGrpc;
//import org.example.gradletranslation.audioRequest;
//import org.example.gradletranslation.audioResponse;
//
//import javax.sound.sampled.AudioFormat;
//import javax.sound.sampled.AudioInputStream;
//import javax.sound.sampled.AudioSystem;
//import javax.sound.sampled.UnsupportedAudioFileException;
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//public class client_two {
//
//    private final GreeterGrpc.GreeterStub asyncStub;
//
//    public client_two(String host, int port) {
//        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
//                .usePlaintext()
//                .build();
//        asyncStub = GreeterGrpc.newStub(channel);
//    }
//
//    public void sendAudioRequest(int[] audioData, TranscriptionCallback callback) {
//        List<Integer> audioByteList = new ArrayList<>();
//        for (int data : audioData) {
//            audioByteList.add(data);
//        }
//        audioRequest request = audioRequest.newBuilder()
//                .addAllAudioByte(audioByteList)
//                .build();
//
//        asyncStub.sendRequest(request, new StreamObserver<audioResponse>() {
//            private StringBuilder transcriptBuilder = new StringBuilder();
//
//            @Override
//            public void onNext(audioResponse response) {
//                transcriptBuilder.append(response.getTranscript()).append(" ");
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                callback.onTranscriptionReceived("Error: " + t.getMessage());
//            }
//
//            @Override
//            public void onCompleted() {
//                callback.onTranscriptionReceived(transcriptBuilder.toString());
//            }
//        });
//    }
//
//    public static int[] readAudioFile(String filePath) throws IOException, UnsupportedAudioFileException {
//        File audioFile = new File(filePath);
//        AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
//        AudioFormat audioFormat = audioStream.getFormat();
//        if (audioFormat.getSampleSizeInBits() != 16 || audioFormat.getChannels() != 1) {
//            throw new UnsupportedAudioFileException("Unsupported audio format. Only mono 16-bit PCM audio is supported.");
//        }
//        int audioDataLength = (int) audioStream.getFrameLength() * audioFormat.getFrameSize();
//        byte[] audioBytes = new byte[audioDataLength];
//        audioStream.read(audioBytes);
//        audioStream.close();
//
//        int[] audioData = new int[audioBytes.length / 2];
//        for (int i = 0; i < audioData.length; i++) {
//            audioData[i] = (audioBytes[2 * i + 1] << 8) | (audioBytes[2 * i] & 0xFF);
//        }
//
//        return audioData;
//    }
//    public interface TranscriptionCallback {
//        void onTranscriptionReceived(String transcription);
//    }
//}
package org.example.bidirectional;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.gradletranslation.GreeterGrpc;
import org.example.gradletranslation.audioRequest;
import org.example.gradletranslation.audioResponse;
import io.grpc.stub.StreamObserver;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class client_two {

    private final GreeterGrpc.GreeterStub asyncStub;

    public client_two(String host, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        asyncStub = GreeterGrpc.newStub(channel);
    }

    public String sendAudioRequest(int[] audioData) throws InterruptedException {
        List<Integer> audioByteList = new ArrayList<>();
        for (int data : audioData) {
            audioByteList.add(data);
        }
        audioRequest request = audioRequest.newBuilder()
                .addAllAudioByte(audioByteList)
                .build();

        CompletableFuture<String> future = new CompletableFuture<>();

        asyncStub.sendRequest(request, new StreamObserver<audioResponse>() {
            private StringBuilder transcriptBuilder = new StringBuilder();

            @Override
            public void onNext(audioResponse response) {
                transcriptBuilder.append(response.getTranscript()).append(" ");
            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }

            @Override
            public void onCompleted() {
                future.complete(transcriptBuilder.toString());
            }
        });

        return future.join(); // Wait for the result and return the transcription
    }

    public static int[] readAudioFile(String filePath) throws IOException, UnsupportedAudioFileException {
        File audioFile = new File(filePath);
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
        AudioFormat audioFormat = audioStream.getFormat();
        if (audioFormat.getSampleSizeInBits() != 16 || audioFormat.getChannels() != 1) {
            throw new UnsupportedAudioFileException("Unsupported audio format. Only mono 16-bit PCM audio is supported.");
        }
        int audioDataLength = (int) audioStream.getFrameLength() * audioFormat.getFrameSize();
        byte[] audioBytes = new byte[audioDataLength];
        audioStream.read(audioBytes);
        audioStream.close();

        int[] audioData = new int[audioBytes.length / 2];
        for (int i = 0; i < audioData.length; i++) {
            audioData[i] = (audioBytes[2 * i + 1] << 8) | (audioBytes[2 * i] & 0xFF);
        }

        return audioData;
    }
}
