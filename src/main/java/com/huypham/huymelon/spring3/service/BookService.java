package com.huypham.huymelon.spring3.service;

import com.huypham.huymelon.spring3.entity.Book;
import com.huypham.huymelon.spring3.entity.User;
import com.huypham.huymelon.spring3.repository.BookRepository;
import com.huypham.huymelon.spring3.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookService {
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public BookService(BookRepository bookRepository, CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Book> getBooksFor(User owner) {
        if (isAdmin(owner)) {
            return bookRepository.findAllWithCategoryAndOwnerOrderByIdDesc();
        }
        return bookRepository.findAllByOwner_IdWithCategoryAndOwnerOrderByIdDesc(owner.getId());
    }

    @Transactional(readOnly = true)
    public String retrieveBookContext(User owner, String question) {
        List<Book> books = bookRepository.findAllByOwner_IdOrderByIdDesc(owner.getId());
        if (books.isEmpty()) return "The user's personal catalog is empty.";
        Set<String> terms = words(question);
        List<RankedBook> ranked = books.stream()
                .map(book -> new RankedBook(book, relevance(book, terms)))
                .sorted(Comparator.comparingInt(RankedBook::score).reversed())
                .limit(8).toList();
        StringBuilder context = new StringBuilder("Books in this user's private catalog (metadata only):\n");
        for (Book book : ranked.stream().map(RankedBook::book).toList()) {
            context.append("- Title: ").append(book.getTitle())
                    .append("; Author: ").append(book.getAuthor())
                    .append("; Category: ").append(book.getCategory().getName())
                    .append("; ISBN: ").append(book.getIsbn())
                    .append("; Year: ").append(book.getPublishYear() == null ? "unknown" : book.getPublishYear())
                    .append("; Copies: ").append(book.getQuantity()).append('\n');
        }
        return context.toString();
    }

    private int relevance(Book book, Set<String> terms) {
        if (terms.isEmpty()) return 0;
        int score = 0;
        for (String term : terms) {
            if (normalize(book.getTitle()).contains(term)) score += 4;
            if (normalize(book.getAuthor()).contains(term)) score += 3;
            if (normalize(book.getCategory().getName()).contains(term)) score += 2;
            if (book.getIsbn() != null && book.getIsbn().toLowerCase(Locale.ROOT).contains(term)) score += 3;
        }
        return score;
    }

    private Set<String> words(String input) {
        return java.util.Arrays.stream(normalize(input).split("[^\\p{L}\\p{N}]+"))
                .filter(word -> word.length() > 1).collect(Collectors.toSet());
    }

    private String normalize(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
    }

    private record RankedBook(Book book, int score) {}

    private boolean isAdmin(User user) {
        if (user == null || user.getRole() == null) return false;
        String role = user.getRole().trim().toUpperCase();
        return "ADMIN".equals(role) || "ROLE_ADMIN".equals(role);
    }

    @Transactional(readOnly = true)
    public long countBooksFor(User owner) {
        if (isAdmin(owner)) {
            return bookRepository.count();
        }
        return bookRepository.countByOwner_Id(owner.getId());
    }

    @Transactional(readOnly = true)
    public Book findByIdForOwner(Long id, User owner) {
        if (isAdmin(owner)) {
            return bookRepository.findByIdWithCategoryAndOwner(id)
                    .orElseThrow(() -> new BookNotFoundException(id));
        }
        return bookRepository.findByIdAndOwner_IdWithCategoryAndOwner(id, owner.getId())
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    public Book create(Book book, User owner) {
        book.setId(null);
        book.setOwner(owner);
        book.setCategory(loadCategory(book.getCategoryId()));
        if (book.getAvailable() == null) {
            book.setAvailable(book.getQuantity() != null && book.getQuantity() > 0);
        }
        return bookRepository.save(book);
    }

    public Book update(Long id, Book changes, User owner) {
        Book book = findByIdForOwner(id, owner);
        book.setIsbn(changes.getIsbn());
        book.setTitle(changes.getTitle());
        book.setAuthor(changes.getAuthor());
        book.setPublishYear(changes.getPublishYear());
        book.setCoverImageUrl(changes.getCoverImageUrl());
        book.setSourceUrl(changes.getSourceUrl());
        book.setQuantity(changes.getQuantity());
        book.setAvailable(changes.getQuantity() != null && changes.getQuantity() > 0);
        book.setCategory(loadCategory(changes.getCategoryId()));
        return bookRepository.save(book);
    }

    public void delete(Long id, User owner) {
        Book book = findByIdForOwner(id, owner);
        bookRepository.delete(book);
    }

    private com.huypham.huymelon.spring3.entity.Category loadCategory(Integer categoryId) {
        if (categoryId == null) throw new IllegalArgumentException("Please select a category.");
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Selected category does not exist."));
    }
}
