package com.enigma.projectstylus.service.blob;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class BlobStorageService {

    @Value("${upstash.blob.url}")
    private String publicBaseUrl;

    @Value("${upstash.blob.token}")
    private String blobToken;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private JsonNode mintCredentials() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://blob.upstash.io/v1/credentials"))
                .header("Authorization", "Bearer " + blobToken)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to mint Upstash S3 credentials: " + response.body());
        }

        return objectMapper.readTree(response.body());
    }

    public String uploadImage(String key, byte[] imageBytes) throws Exception {
        JsonNode minted = mintCredentials();

        try (S3Client s3 = S3Client.builder()
                .endpointOverride(URI.create(minted.get("endpoint").asText()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsSessionCredentials.create(
                                minted.get("accessKeyId").asText(),
                                minted.get("secretAccessKey").asText(),
                                minted.get("sessionToken").asText())))
                .region(Region.of("auto"))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .chunkedEncodingEnabled(false)
                        .build())
                .build()) {

            s3.putObject(
                    PutObjectRequest.builder()
                            .bucket(minted.get("bucket").asText())
                            .key(key)
                            .contentType("image/jpeg")
                            .build(),
                    RequestBody.fromBytes(imageBytes)
            );
        }

        // Return clean public CDN URL
        String base = publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl;
        return base + "/" + key;
    }

    public void deleteImages(List<String> keys) {
        if (keys == null || keys.isEmpty()) return;

        try {
            JsonNode minted = mintCredentials();

            try (S3Client s3 = S3Client.builder()
                    .endpointOverride(URI.create(minted.get("endpoint").asText()))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsSessionCredentials.create(
                                    minted.get("accessKeyId").asText(),
                                    minted.get("secretAccessKey").asText(),
                                    minted.get("sessionToken").asText())))
                    .region(Region.of("auto"))
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .chunkedEncodingEnabled(false)
                            .build())
                    .build()) {

                for (String key : keys) {
                    s3.deleteObject(builder -> builder
                            .bucket(minted.get("bucket").asText())
                            .key(key));
                }
            }
        } catch (Exception e) {
            // Silently log or ignore cleanup failures so your daily job isn't interrupted
            System.err.println("Failed to delete expired posters: " + e.getMessage());
        }
    }
}