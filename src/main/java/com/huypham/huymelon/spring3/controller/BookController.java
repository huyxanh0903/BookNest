package com.huypham.huymelon.spring3.controller;

import com.huypham.huymelon.spring3.entity.Book;
import com.huypham.huymelon.spring3.entity.User;
import com.huypham.huymelon.spring3.service.CategoryService;
import com.huypham.huymelon.spring3.service.BookService;
import com.huypham.huymelon.spring3.service.ExternalBookSearchService;
import com.huypham.huymelon.spring3.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BookController {
    private final BookService bookService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final ExternalBookSearchService externalBookSearchService;

    public BookController(BookService bookService, UserService userService, CategoryService categoryService,
                          ExternalBookSearchService externalBookSearchService) {
        this.bookService = bookService;
        this.userService = userService;
        this.categoryService = categoryService;
        this.externalBookSearchService = externalBookSearchService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String createAccount(@RequestParam String username,
                                @RequestParam String email,
                                @RequestParam String password,
                                @RequestParam String rePassword,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        username = username.trim();
        email = email.trim();
        if (username.length() < 3 || username.length() > 50) {
            model.addAttribute("error", "Username must be between 3 and 50 characters.");
            return "register";
        }
        if (email.length() > 255 || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            model.addAttribute("error", "Please enter a valid email address.");
            return "register";
        }
        if (password.length() < 8) {
            model.addAttribute("error", "Password must be at least 8 characters long.");
            return "register";
        }
        if (!password.equals(rePassword)) {
            model.addAttribute("error", "The passwords do not match.");
            return "register";
        }
        if (userService.usernameExists(username)) {
            model.addAttribute("error", "That username is already in use.");
            return "register";
        }
        if (userService.emailExists(email)) {
            model.addAttribute("error", "That email address is already registered.");
            return "register";
        }
        userService.register(username, email, password);
        redirectAttributes.addFlashAttribute("success", "Your account was created successfully. You can sign in now.");
        return "redirect:/login";
    }

    @GetMapping({"/", "/index"})
    public String index(Model model, @AuthenticationPrincipal UserDetails principal) {
        User owner = currentUser(principal);
        model.addAttribute("bookCount", bookService.countBooksFor(owner));
        return "index";
    }

    @GetMapping("/book")
    public String getAllBook(Model model, @AuthenticationPrincipal UserDetails principal) {
        model.addAttribute("List", bookService.getBooksFor(currentUser(principal)));
        return "books";
    }

    @GetMapping("/book/new")
    public String newBook(@RequestParam(required = false) String isbn,
                          @RequestParam(required = false) String title,
                          @RequestParam(required = false) String author,
                          @RequestParam(required = false) Integer publishYear,
                          @RequestParam(required = false) String coverImageUrl,
                          @RequestParam(required = false) String sourceUrl,
                          Model model) {
        Book book = new Book();
        book.setIsbn(isbn);
        book.setTitle(title);
        book.setAuthor(author);
        book.setPublishYear(publishYear);
        book.setCoverImageUrl(coverImageUrl);
        book.setSourceUrl(sourceUrl);
        book.setQuantity(1);
        book.setAvailable(true);
        model.addAttribute("object", book);
        model.addAttribute("isNew", true);
        model.addAttribute("categories", categoryService.findAll());
        return "edit";
    }

    @GetMapping("/book/search")
    public String searchBooks(@RequestParam(required = false, defaultValue = "") String q, Model model) {
        String query = q.trim();
        model.addAttribute("query", query);
        model.addAttribute("results", java.util.List.of());
        if (!query.isBlank()) {
            try {
                model.addAttribute("results", externalBookSearchService.search(query));
            } catch (RestClientException exception) {
                model.addAttribute("searchError", "Book search is temporarily unavailable. Please try again.");
            }
        }
        return "book-search";
    }

    @PostMapping("/book")
    public String createBook(@ModelAttribute("object") Book form,
                             Model model,
                             @AuthenticationPrincipal UserDetails principal,
                             RedirectAttributes redirectAttributes) {
        String error = validateBook(form);
        if (error != null) {
            model.addAttribute("isNew", true);
            model.addAttribute("error", error);
            model.addAttribute("categories", categoryService.findAll());
            return "edit";
        }
        try {
            bookService.create(form, currentUser(principal));
        } catch (IllegalArgumentException ex) {
            model.addAttribute("isNew", true);
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            return "edit";
        }
        redirectAttributes.addFlashAttribute("notice", "Book added to your library.");
        return "redirect:/book";
    }

    @GetMapping("/book/edit/{id}")
    public String editBook(@PathVariable Long id,
                           Model model,
                           @AuthenticationPrincipal UserDetails principal) {
        Book book = bookService.findByIdForOwner(id, currentUser(principal));
        book.setCategoryId(book.getCategory().getId());
        model.addAttribute("object", book);
        model.addAttribute("isNew", false);
        model.addAttribute("categories", categoryService.findAll());
        return "edit";
    }

    @GetMapping("/book/{id}")
    public String viewBook(@PathVariable Long id,
                           Model model,
                           @AuthenticationPrincipal UserDetails principal) {
        Book book = bookService.findByIdForOwner(id, currentUser(principal));
        model.addAttribute("book", book);
        String query = book.getIsbn() == null || book.getIsbn().isBlank()
                ? book.getTitle() + " " + book.getAuthor()
                : book.getIsbn();
        model.addAttribute("openLibrarySearchUrl", "https://openlibrary.org/search?q="
                + java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8));
        return "book-detail";
    }

    @PostMapping("/book/{id}/update")
    public String updateBook(@PathVariable Long id,
                             @ModelAttribute("object") Book form,
                             Model model,
                             @AuthenticationPrincipal UserDetails principal,
                             RedirectAttributes redirectAttributes) {
        String error = validateBook(form);
        if (error != null) {
            form.setId(id);
            model.addAttribute("isNew", false);
            model.addAttribute("error", error);
            model.addAttribute("categories", categoryService.findAll());
            return "edit";
        }
        try {
            bookService.update(id, form, currentUser(principal));
        } catch (IllegalArgumentException ex) {
            form.setId(id);
            model.addAttribute("isNew", false);
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            return "edit";
        }
        redirectAttributes.addFlashAttribute("notice", "Book details updated.");
        return "redirect:/book";
    }

    @PostMapping("/book/{id}/delete")
    public String deleteBook(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails principal,
                             RedirectAttributes redirectAttributes) {
        bookService.delete(id, currentUser(principal));
        redirectAttributes.addFlashAttribute("notice", "Book removed from your library.");
        return "redirect:/book";
    }

    private User currentUser(UserDetails principal) {
        return userService.findByUsername(principal.getUsername());
    }

    private String validateBook(Book book) {
        if (book.getIsbn() == null || book.getIsbn().isBlank() || book.getIsbn().length() > 20) {
            return "ISBN is required and must be 20 characters or fewer.";
        }
        if (book.getTitle() == null || book.getTitle().isBlank() || book.getTitle().length() > 200) {
            return "Title is required and must be 200 characters or fewer.";
        }
        if (book.getAuthor() == null || book.getAuthor().isBlank() || book.getAuthor().length() > 100) {
            return "Author is required and must be 100 characters or fewer.";
        }
        if (book.getQuantity() == null || book.getQuantity() < 0) {
            return "Copies must be zero or more.";
        }
        if (book.getCategoryId() == null) {
            return "Please select a category.";
        }
        book.setIsbn(book.getIsbn().trim());
        book.setTitle(book.getTitle().trim());
        book.setAuthor(book.getAuthor().trim());
        if (book.getCoverImageUrl() != null) {
            book.setCoverImageUrl(book.getCoverImageUrl().trim());
            if (book.getCoverImageUrl().isBlank()) book.setCoverImageUrl(null);
            else if (book.getCoverImageUrl().length() > 1000) return "Cover image URL must be 1,000 characters or fewer.";
            else if (!book.getCoverImageUrl().matches("(?i)^https?://.*")) return "Cover image URL must start with http:// or https://.";
        }
        if (book.getSourceUrl() != null) {
            book.setSourceUrl(book.getSourceUrl().trim());
            if (book.getSourceUrl().isBlank()) book.setSourceUrl(null);
            else if (book.getSourceUrl().length() > 1000 || !book.getSourceUrl().matches("(?i)^https?://.*")) return "Source URL must be a valid HTTP or HTTPS link of 1,000 characters or fewer.";
        }
        return null;
    }
}
