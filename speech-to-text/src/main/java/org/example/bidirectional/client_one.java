package org.example.bidirectional;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.example.gradletranslation.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import com.google.gson.Gson;
import com.google.gson.JsonObject;


public class client_one {

    private final GreeterGrpc.GreeterStub asyncStub;

    public client_one(String host, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        asyncStub = GreeterGrpc.newStub(channel);
    }

    public void sendAudioRequest(int[] audioData) throws InterruptedException {
        // Convert the int[] array to a List<Integer> for the audioRequest
        List<Integer> audioByteList = new ArrayList<>();
        for (int data : audioData) {
            audioByteList.add(data);
        }
        audioRequest request = audioRequest.newBuilder()
                .addAllAudioByte(audioByteList)
                .build();

        CountDownLatch latch = new CountDownLatch(1);

        // Send the request asynchronously and receive responses using StreamObserver
        asyncStub.sendRequest(request, new StreamObserver<audioResponse>() {
            @Override
            public void onNext(audioResponse response) {
                System.out.println("Received transcript: " + response.getTranscript());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error: " + t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Stream completed.");
                latch.countDown();
            }
        });
        latch.await();
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


    public static void main(String[] args) throws IOException, InterruptedException {
        String host = "127.0.0.1";
        int port = 9090;

        client_one client = new client_one(host, port);
        String audioFilePath = "/Users/saanvinair/IdeaProjects/gradle-translation/speech-to-text/test_sample_audio.wav";

        try {
            int[] audioData = readAudioFile(audioFilePath);
            client.sendAudioRequest(audioData);
        }catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
