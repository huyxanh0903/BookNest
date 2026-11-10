package com.huypham.huymelon.spring3.controller;

import com.huypham.huymelon.spring3.service.AiChatService;
import com.huypham.huymelon.spring3.service.BookService;
import com.huypham.huymelon.spring3.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class AiChatController {
    private final AiChatService aiChatService;
    private final BookService bookService;
    private final UserService userService;
    public AiChatController(AiChatService aiChatService, BookService bookService, UserService userService) {
        this.aiChatService = aiChatService;
        this.bookService = bookService;
        this.userService = userService;
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/ai/chat")
    public String chat(HttpSession session, Model model) {
        List<ChatMessage> visible = (List<ChatMessage>) session.getAttribute("booknestAiVisible");
        Boolean useRag = (Boolean) session.getAttribute("booknestAiUseRag");
        if (useRag == null) useRag = true;
        model.addAttribute("messages", visible == null ? List.of() : visible);
        model.addAttribute("aiConfigured", aiChatService.isConfigured());
        model.addAttribute("useRag", useRag);
        return "ai-chat";
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/ai/chat")
    public String send(@RequestParam String message,
                       @RequestParam(name = "useRag", required = false, defaultValue = "true") boolean useRag,
                       HttpSession session, RedirectAttributes redirect,
                       @AuthenticationPrincipal Object principal) {
        try {
            submit(message, useRag, session, principal);
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("chatError", e.getMessage());
        }
        return "redirect:/ai/chat";
    }

    @PostMapping("/ai/chat/clear")
    public String clear(HttpSession session) {
        session.removeAttribute("booknestAiContext");
        session.removeAttribute("booknestAiVisible");
        return "redirect:/ai/chat";
    }

    @PostMapping("/ai/chat/ask")
    @ResponseBody
    public Map<String, String> ask(@RequestParam String message,
                                   @RequestParam(name = "useRag", required = false, defaultValue = "true") boolean useRag,
                                   HttpSession session,
                                   @AuthenticationPrincipal Object principal) {
        try {
            return Map.of("answer", submit(message, useRag, session, principal));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Map.of("error", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private String submit(String message, boolean useRag, HttpSession session, Object principal) {
        String prompt = message == null ? "" : message.trim();
        if (prompt.isBlank() || prompt.length() > 20000) throw new IllegalArgumentException("Enter a message of up to 20,000 characters.");
        List<Map<String, String>> context = (List<Map<String, String>>) session.getAttribute("booknestAiContext");
        List<ChatMessage> visible = (List<ChatMessage>) session.getAttribute("booknestAiVisible");
        if (context == null) { context = new ArrayList<>(); session.setAttribute("booknestAiContext", context); }
        if (visible == null) { visible = new ArrayList<>(); session.setAttribute("booknestAiVisible", visible); }
        session.setAttribute("booknestAiUseRag", useRag);

        while (context.size() > 20) context.remove(0);
        while (visible.size() > 20) visible.remove(0);
        context.add(Map.of("role", "user", "content", prompt));
        visible.add(new ChatMessage("You", prompt, true, null));
        try {
            String answer;
            if (useRag) {
                String catalogContext = bookService.retrieveBookContext(userService.getCurrentUser(principal), prompt);
                answer = aiChatService.reply(context, catalogContext, true);
                visible.add(new ChatMessage("BookNest AI", answer, false, true));
            } else {
                answer = aiChatService.reply(context, "", false);
                visible.add(new ChatMessage("BookNest AI", answer, false, false));
            }
            context.add(Map.of("role", "assistant", "content", answer));
            return answer;
        } catch (IllegalStateException e) {
            context.remove(context.size() - 1);
            visible.remove(visible.size() - 1);
            throw e;
        }
    }

    public record ChatMessage(String speaker, String content, boolean user, Boolean ragMode) {
        public ChatMessage(String speaker, String content, boolean user) {
            this(speaker, content, user, null);
        }
    }
}
