package com.huypham.huymelon.spring3.controller;

import com.huypham.huymelon.spring3.entity.User;
import com.huypham.huymelon.spring3.service.AiChatService;
import com.huypham.huymelon.spring3.service.ExternalBookSearchService;
import com.huypham.huymelon.spring3.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class ReadingProfileController {
    private final UserService userService;
    private final ExternalBookSearchService searchService;
    private final AiChatService aiChatService;

    public ReadingProfileController(UserService userService, ExternalBookSearchService searchService, AiChatService aiChatService) {
        this.userService = userService;
        this.searchService = searchService;
        this.aiChatService = aiChatService;
    }

    @GetMapping("/reading-profile")
    public String profile(@AuthenticationPrincipal Object principal, Model model) {
        model.addAttribute("profile", userService.getCurrentUser(principal));
        return "reading-profile";
    }

    @PostMapping("/reading-profile")
    public String save(@AuthenticationPrincipal Object principal,
                       @RequestParam(required = false) String favoriteGenres,
                       @RequestParam(required = false) String readingTopics,
                       @RequestParam(required = false) String preferredLanguage,
                       @RequestParam(required = false) String readingGoal,
                       RedirectAttributes redirect) {
        try {
            userService.saveReadingProfile(userService.getCurrentUser(principal), favoriteGenres,
                    readingTopics, preferredLanguage, readingGoal);
            redirect.addFlashAttribute("profileSaved", "Your reading profile has been saved.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("profileError", e.getMessage());
        }
        return "redirect:/reading-profile";
    }

    @GetMapping("/recommendations")
    public String recommendations(@AuthenticationPrincipal Object principal, Model model) {
        User user = userService.getCurrentUser(principal);
        model.addAttribute("profile", user);
        String searchQuery = Stream.of(user.getFavoriteGenres(), user.getReadingTopics(), user.getPreferredLanguage())
                .filter(value -> value != null && !value.isBlank()).collect(Collectors.joining(" "));
        if (searchQuery.isBlank()) {
            model.addAttribute("profileNeeded", true);
            return "recommendations";
        }
        try {
            List<ExternalBookSearchService.SearchResult> results = searchService.search(searchQuery);
            model.addAttribute("results", results.stream().limit(8).toList());
            String candidates = results.stream().limit(8).map(book -> "Title: " + book.title() + "; Author: " + book.author()
                    + "; Year: " + book.publishYear() + "; ISBN: " + book.isbn()).collect(Collectors.joining("\n"));
            String profileText = "Favorite genres: " + user.getFavoriteGenres() + "\nTopics: " + user.getReadingTopics()
                    + "\nPreferred language: " + user.getPreferredLanguage() + "\nReading goal: " + user.getReadingGoal()
                    + "\nCandidate books from external catalogs:\n" + (candidates.isBlank() ? "No candidates found." : candidates);
            model.addAttribute("explanation", aiChatService.recommend("Recommend the best matches for my reading profile and explain briefly why.", profileText));
        } catch (RestClientException e) {
            model.addAttribute("recommendationError", "External book search is temporarily unavailable. Please try again.");
        } catch (IllegalStateException e) {
            model.addAttribute("recommendationError", e.getMessage());
        }
        return "recommendations";
    }
}
