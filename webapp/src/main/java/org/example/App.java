package org.example;

import org.example.bidirectional.client_two;
import spark.Spark;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class App {

    public static void main(String[] args) {
        Spark.port(8060);

        // Configure multipart handling
        Spark.before("/upload", (req, res) -> {
            // Set multipart config for servlet
            req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/tmp"));
        });

        // Serve the upload form
        Spark.get("/", (req, res) -> {
            return "<html><body><h2>Upload a WAV file:</h2>" +
                    "<form method='post' action='/upload' enctype='multipart/form-data'>" +
                    "<input type='file' name='wavFile'>" +
                    "<button type='submit'>Upload</button>" +
                    "</form></body></html>";
        });

        // Handle file upload and transcription
        Spark.post("/upload", (req, res) -> {
            try {
                // Retrieve uploaded file
                Part filePart = req.raw().getPart("wavFile");
                String fileName = filePart.getSubmittedFileName();

                // Create a temporary file to store the uploaded file
                Path tempFile = Files.createTempFile("uploaded", ".wav");

                try (InputStream input = filePart.getInputStream()) {
                    // Copy the uploaded file to the temporary file
                    Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("File uploaded successfully: " + fileName);

                    // Initialize gRPC client
                    String host = "localhost";
                    int port = 9090;
                    client_two client = new client_two(host, port);

                    // Read audio data from file
                    int[] audioData = client_two.readAudioFile(tempFile.toString());

                    // Send audio data for transcription asynchronously
                    String transcription = client.sendAudioRequest(audioData);

                    // Set the response content type and body
                    res.type("text/html");
                    res.status(200);
                    res.body("<html><body><h2>Transcription:</h2><p>" + transcription + "</p></body></html>");

                    return res.body(); // Return the response body immediately
                } finally {
                    Files.deleteIfExists(tempFile);
                }
            } catch (Exception e) {
                System.err.println("Error handling file upload");
                e.printStackTrace();
                res.status(500); // Set HTTP response status to 500 Internal Server Error
                return "Error handling file upload";
            }
        });

        // Configure global exception handling for uncaught exceptions
        Spark.exception(Exception.class, (e, req, res) -> {
            System.err.println("Unexpected error occurred");
            e.printStackTrace();
            res.status(500);
            res.body("Unexpected error occurred");
        });
    }
}
