package com.huypham.huymelon.spring3.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

@Service
public class AiChatService {
    private final RestClient client = RestClient.create();
    private final String apiKey;
    private final String endpoint;
    private final String model;

    public AiChatService(@Value("${booknest.ai.api-key:}") String apiKey,
                         @Value("${booknest.ai.endpoint:https://api.openai.com/v1/chat/completions}") String endpoint,
                         @Value("${booknest.ai.model:gpt-4o-mini}") String model) {
        this.apiKey = apiKey;
        this.endpoint = endpoint;
        this.model = model;
    }

    public boolean isConfigured() { return apiKey != null && !apiKey.isBlank(); }

    public String reply(List<Map<String, String>> history, String bookContext) {
        return complete(history, "You are BookNest's helpful book catalog assistant. " +
                "Answer in the user's language. Use the supplied catalog context only for claims about books owned by the user. " +
                "The context contains metadata, not book contents; never claim to have read the books. " +
                "If the catalog context does not support an answer, say so clearly.\n\n" + bookContext);
    }

    public String recommend(String prompt, String recommendationContext) {
        return complete(List.of(Map.of("role", "user", "content", prompt)),
                "You are BookNest's reading recommendation assistant. Answer in the user's language. " +
                        "Recommend only books present in the candidate list below. Give concise reasons based on the user's profile and candidate metadata. " +
                        "Do not claim knowledge of book contents beyond the metadata. If there are no candidates, say so clearly.\n\n" + recommendationContext);
    }

    private String complete(List<Map<String, String>> history, String systemPrompt) {
        if (!isConfigured()) throw new IllegalStateException("AI is not configured. Set BOOKNEST_AI_API_KEY and restart the app.");
        try {
            List<Map<String, String>> messages = new java.util.ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.addAll(history);
            JsonNode response = client.post().uri(endpoint)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("model", model, "messages", messages, "temperature", 0.7))
                    .retrieve().body(JsonNode.class);
            String answer = response == null ? "" : response.path("choices").path(0).path("message").path("content").asText("");
            if (answer.isBlank()) throw new IllegalStateException("AI provider returned an empty response.");
            return answer;
        } catch (RestClientException e) {
            throw new IllegalStateException("Could not reach the AI provider. Check your API key, endpoint, and model settings.", e);
        }
    }
}
