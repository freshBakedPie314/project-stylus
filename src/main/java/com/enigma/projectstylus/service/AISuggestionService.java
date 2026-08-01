package com.enigma.projectstylus.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class AISuggestionService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    // gemini-2.5-flash
    private final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent";
    public AISuggestionService(ObjectMapper objectMapper, SimpMessagingTemplate messagingTemplate) {
        this.restClient = RestClient.create();
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
    }

    @Async
    public void generateAndBroadcastSuggestions(String roomId, String userId, String movie, String description) {
        try {
            String cleanPrompt = """
                You are a sarcastic comedian playing a party game called 'Describe Movies Badly'.
                
                Target Movie Title: %s
                Official TMDB Plot Synopsis: %s
                
                Instructions:
                1. Read the provided official TMDB plot synopsis and generate exactly 3 alternative funny, hilariously inaccurate, or extremely oversimplified clickbait descriptions.
                2. Write them in modern internet humor format (e.g., YouTube titles like 'BOOMER goes to space, gets stuck behind a bookshelf', or 'Rich orphan punches mentally ill clown').
                3. Ensure they match the provided JSON schema precisely. Do not include markdown code ticks like ```json.
                4. If the movie synopsis or title makes no sense, return an empty array list.
                """.formatted(movie, description != null ? description : "No overview available.");

            // Build dynamic request payload requiring structured JSON configurations
            String payload = """
                {
                  "contents": [{
                    "parts": [{"text": "%s"}]
                  }],
                  "generationConfig": {
                    "responseMimeType": "application/json",
                    "responseSchema": {
                      "type": "OBJECT",
                      "properties": {
                        "suggestions": {
                          "type": "ARRAY",
                          "items": { "type": "STRING" }
                        }
                      },
                      "required": ["suggestions"]
                    }
                  }
                }
                """.formatted(cleanPrompt.replace("\"", "\\\"").replace("\n", " "));

            // Execute synchronous block inside the async worker wrapper safely
            String rawResponse = restClient.post()
                    .uri(BASE_URL + "?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            // Parse out deep Gemini tree nodes using Jackson's tree model lookup
            String jsonText = objectMapper.readTree(rawResponse)
                    .path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            // Convert string value mapping target properties cleanly to list container
            GeminiMovieSuggestions parsed = objectMapper.readValue(jsonText, GeminiMovieSuggestions.class);
            List<String> suggestionsList = parsed.suggestions() != null ? parsed.suggestions() : Collections.emptyList();

            //System.out.println("[AI SUGGESTION] Gemini Generated Suggestions for " + movie + ": " + suggestionsList);

            // Structure real-time WebSocket outbox metadata envelope wrapper
            Map<String, Object> wsPayload = Map.of(
                    "type", "AI_SUGGESTIONS",
                    "movieName", movie,
                    "suggestions", suggestionsList
            );

            // Transmit results directly down to the target user's secure private channel sub
            messagingTemplate.convertAndSend("/topic/" + roomId + "/user/" + userId, (Object)wsPayload);

        } catch (Exception e) {
            // Defensive recovery logic block ensuring thread failures drop cleanly without freezing gameplay state
            System.err.println("Async AI Suggestion Pipeline dropped frame processing: " + e.getMessage());
        }
    }

    // Explicit DTO wrapper record mapping for clean object unmarshalling reflection steps
    private record GeminiMovieSuggestions(List<String> suggestions) {}
}