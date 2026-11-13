package com.huypham.huymelon.spring3.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);

    private final RestClient client = RestClient.create();

    // Primary model
    private final String apiKey;
    private final String endpoint;
    private final String model;

    // Fallback model
    private final String fallbackApiKey;
    private final String fallbackEndpoint;
    private final String fallbackModel;

    public AiChatService(
            @Value("${booknest.ai.api-key:}") String apiKey,
            @Value("${booknest.ai.endpoint:https://api.groq.com/openai/v1/chat/completions}") String endpoint,
            @Value("${booknest.ai.model:openai/gpt-oss-20b}") String model,
            @Value("${booknest.ai.fallback.api-key:}") String fallbackApiKey,
            @Value("${booknest.ai.fallback.endpoint:https://api.groq.com/openai/v1/chat/completions}") String fallbackEndpoint,
            @Value("${booknest.ai.fallback.model:llama3-8b-8192}") String fallbackModel) {
        this.apiKey = apiKey;
        this.endpoint = endpoint;
        this.model = model;
        this.fallbackApiKey = fallbackApiKey.isBlank() ? apiKey : fallbackApiKey;
        this.fallbackEndpoint = fallbackEndpoint;
        this.fallbackModel = fallbackModel;
    }

    public boolean isConfigured() { return apiKey != null && !apiKey.isBlank(); }

    /** Display-friendly label for each model slot. */
    public String primaryModelLabel()  { return friendlyName(model); }
    public String fallbackModelLabel() { return friendlyName(fallbackModel); }

    private String friendlyName(String m) {
        if (m == null || m.isBlank()) return "AI";
        // Strip org prefix e.g. "openai/gpt-oss-20b" → "gpt-oss-20b"
        String base = m.contains("/") ? m.substring(m.lastIndexOf('/') + 1) : m;
        return base.toUpperCase();
    }

    public String reply(List<Map<String, String>> history, String bookContext, boolean useRag) {
        return reply(history, bookContext, useRag, false);
    }

    public String reply(List<Map<String, String>> history, String bookContext, boolean useRag, boolean useFallback) {
        String systemPrompt;
        if (useRag) {
            systemPrompt = "You are BookNest's personal book catalog assistant. " +
                    "Answer in the user's language. Use ONLY the supplied catalog context below to answer. " +
                    "The context contains metadata of books owned by the user. " +
                    "If the user asks about a book or topic that does not exist in the supplied catalog context, politely and clearly state that it is not present in their personal collection. " +
                    "Do NOT invent or fabricate books outside this catalog context.\n\n" + bookContext;
        } else {
            systemPrompt = "You are BookNest's knowledgeable literary and reading assistant. " +
                    "Answer in the user's language. You have open, broad knowledge about all books, literature, famous authors, history of writing, summaries, and book recommendations from around the world. " +
                    "Feel free to discuss any book, author, or topic outside of the user's local collection.";
        }
        return complete(history, systemPrompt, useFallback);
    }

    public String recommend(String prompt, String recommendationContext) {
        return complete(List.of(Map.of("role", "user", "content", prompt)),
                "You are BookNest's reading recommendation assistant. Answer in the user's language. " +
                        "Recommend only books present in the candidate list below. Give concise reasons based on the user's profile and candidate metadata. " +
                        "Do not claim knowledge of book contents beyond the metadata. If there are no candidates, say so clearly.\n\n" + recommendationContext,
                false);
    }

    private String complete(List<Map<String, String>> history, String systemPrompt, boolean useFallback) {
        if (!isConfigured()) throw new IllegalStateException("AI is not configured. Set BOOKNEST_AI_API_KEY and restart the app.");

        String chosenKey      = useFallback ? fallbackApiKey      : apiKey;
        String chosenEndpoint = useFallback ? fallbackEndpoint     : endpoint;
        String chosenModel    = useFallback ? fallbackModel        : model;

        try {
            return callProvider(history, systemPrompt, chosenKey, chosenEndpoint, chosenModel);
        } catch (IllegalStateException | RestClientException primary) {
            if (!useFallback) {
                // Auto-fallback: try the second model automatically
                log.warn("Primary model '{}' failed ({}). Auto-switching to fallback model '{}'.",
                        model, primary.getMessage(), fallbackModel);
                try {
                    return callProvider(history, systemPrompt, fallbackApiKey, fallbackEndpoint, fallbackModel);
                } catch (IllegalStateException | RestClientException fallback) {
                    throw new IllegalStateException(
                            "Both AI models are unavailable. Primary: " + primary.getMessage()
                            + " | Fallback: " + fallback.getMessage(), fallback);
                }
            }
            throw primary instanceof IllegalStateException
                    ? (IllegalStateException) primary
                    : new IllegalStateException("Could not reach the AI provider.", primary);
        }
    }

    private String callProvider(List<Map<String, String>> history, String systemPrompt,
                                String key, String ep, String mdl) {
        try {
            List<Map<String, String>> messages = new java.util.ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.addAll(history);
            JsonNode response = client.post().uri(ep)
                    .header("Authorization", "Bearer " + key)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("model", mdl, "messages", messages, "temperature", 0.7))
                    .retrieve().body(JsonNode.class);
            String answer = response == null ? "" : response.path("choices").path(0).path("message").path("content").asText("");
            if (answer.isBlank()) throw new IllegalStateException("AI provider returned an empty response.");
            return answer;
        } catch (RestClientException e) {
            throw new IllegalStateException("Could not reach the AI provider (" + mdl + ").", e);
        }
    }
}
