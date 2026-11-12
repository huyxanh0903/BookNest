package com.huypham.huymelon.spring3.service;

import tools.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExternalBookSearchService {
    private static final Logger log = LoggerFactory.getLogger(ExternalBookSearchService.class);
    private final RestClient restClient = RestClient.create();

    public List<SearchResult> search(String query) {
        try {
            List<SearchResult> googleResults = searchGoogleBooks(query);
            if (!googleResults.isEmpty()) return googleResults;
            return searchOpenLibrary(query);
        } catch (RestClientException googleError) {
            log.warn("Google Books search failed; trying Open Library. Cause: {}", googleError.getMessage());
            try {
                return searchOpenLibrary(query);
            } catch (RestClientException openLibraryError) {
                log.error("Both book search providers failed. Google Books: {}; Open Library: {}",
                        googleError.getMessage(), openLibraryError.getMessage());
                throw openLibraryError;
            }
        }
    }

    private List<SearchResult> searchGoogleBooks(String query) {
        URI uri = UriComponentsBuilder.fromUriString("https://www.googleapis.com/books/v1/volumes")
                .queryParam("q", query)
                .queryParam("maxResults", 20)
                .build()
                .encode()
                .toUri();

        JsonNode response = restClient.get().uri(uri).retrieve().body(JsonNode.class);
        List<SearchResult> results = new ArrayList<>();
        if (response == null || !response.path("items").isArray()) return results;

        for (JsonNode item : response.path("items")) {
            JsonNode info = item.path("volumeInfo");
            String title = text(info, "title");
            if (title.isBlank()) continue;
            String authors = authors(info.path("authors"));
            String isbn = isbn(info.path("industryIdentifiers"));
            Integer year = year(text(info, "publishedDate"));
            String thumbnail = text(info.path("imageLinks"), "thumbnail");
            String infoLink = text(info, "infoLink");
            results.add(new SearchResult(title, authors, isbn, year, thumbnail, infoLink));
        }
        return results;
    }

    private List<SearchResult> searchOpenLibrary(String query) {
        URI uri = UriComponentsBuilder.fromUriString("https://openlibrary.org/search.json")
                .queryParam("q", query)
                .queryParam("fields", "key,title,author_name,first_publish_year,isbn,cover_i")
                .queryParam("limit", 20)
                .build()
                .encode()
                .toUri();

        JsonNode response = restClient.get().uri(uri)
                .header("User-Agent", "BookNest/1.0 (book search feature)")
                .retrieve().body(JsonNode.class);
        List<SearchResult> results = new ArrayList<>();
        if (response == null || !response.path("docs").isArray()) return results;

        for (JsonNode item : response.path("docs")) {
            String title = text(item, "title");
            if (title.isBlank()) continue;
            String key = text(item, "key");
            String coverId = item.path("cover_i").asText("");
            String coverUrl = coverId.isBlank() ? "" : "https://covers.openlibrary.org/b/id/" + coverId + "-M.jpg";
            results.add(new SearchResult(title, authors(item.path("author_name")), firstIsbn(item.path("isbn")),
                    item.path("first_publish_year").isNumber() ? item.path("first_publish_year").asInt() : null,
                    coverUrl, key.isBlank() ? "" : "https://openlibrary.org" + key));
        }
        return results;
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? "" : value.asText("");
    }

    private String authors(JsonNode authors) {
        if (!authors.isArray() || authors.isEmpty()) return "Unknown author";
        List<String> names = new ArrayList<>();
        authors.forEach(author -> names.add(author.asText()));
        return String.join(", ", names);
    }

    private String isbn(JsonNode identifiers) {
        if (!identifiers.isArray()) return "";
        String isbn10 = "";
        for (JsonNode identifier : identifiers) {
            String type = text(identifier, "type");
            String value = text(identifier, "identifier");
            if ("ISBN_13".equals(type)) return value;
            if ("ISBN_10".equals(type)) isbn10 = value;
        }
        return isbn10;
    }

    private String firstIsbn(JsonNode identifiers) {
        return identifiers.isArray() && !identifiers.isEmpty() ? identifiers.get(0).asText("") : "";
    }

    private Integer year(String publishedDate) {
        if (publishedDate == null || publishedDate.isBlank()) return null;
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\b(\\d{4})\\b").matcher(publishedDate);
        if (matcher.find()) {
            try {
                return Integer.valueOf(matcher.group(1));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    public record SearchResult(String title, String author, String isbn,
                               Integer publishYear, String thumbnail, String infoLink) {
    }
}
