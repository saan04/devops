package org.example;

import spark.Request;
import spark.Response;
import spark.Spark;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javax.servlet.MultipartConfigElement; // Add this import
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        // Set the port for Spark
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

        // Handle file upload
        Spark.post("/upload", (req, res) -> {
            try {
                // Get the submitted file name
                String fileName = req.raw().getPart("wavFile").getSubmittedFileName();
                logger.info("Received file upload request: {}", fileName);

                // Create a temporary file to store the uploaded file
                Path tempFile = Files.createTempFile("uploaded", ".wav");

                try (InputStream input = req.raw().getPart("wavFile").getInputStream()) {
                    // Copy the uploaded file to the temporary file
                    Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
                    String result = "File uploaded successfully: " + fileName;
                    logger.info(result);
                    return result;
                } finally {
                    // Delete the temporary file after processing
                    Files.deleteIfExists(tempFile);
                }
            } catch (Exception e) {
                // Log the error and return an error message
                String errorMessage = "Error handling file upload";
                logger.error(errorMessage, e);
                res.status(500); // Set HTTP response status to 500 Internal Server Error
                return errorMessage;
            }
        });

        // Configure global exception handling for uncaught exceptions
        Spark.exception(Exception.class, (e, req, res) -> {
            // Log unexpected errors and set HTTP response status to 500
            logger.error("Unexpected error occurred", e);
            res.status(500);
            res.body("Unexpected error occurred");
        });
    }
}
