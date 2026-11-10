package com.huypham.huymelon.spring3.service;

import com.huypham.huymelon.spring3.entity.User;
import com.huypham.huymelon.spring3.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsernameIgnoreCase(username.trim());
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmailIgnoreCase(email.trim());
    }

    public User findByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated account was not found."));
    }

    public User saveReadingProfile(User user, String genres, String topics, String language, String goal) {
        user.setFavoriteGenres(cleanProfileField(genres, 500));
        user.setReadingTopics(cleanProfileField(topics, 500));
        user.setPreferredLanguage(cleanProfileField(language, 80));
        user.setReadingGoal(cleanProfileField(goal, 300));
        return userRepository.save(user);
    }

    private String cleanProfileField(String value, int maxLength) {
        if (value == null || value.isBlank()) return null;
        String cleaned = value.trim();
        if (cleaned.length() > maxLength) throw new IllegalArgumentException("Profile field exceeds " + maxLength + " characters.");
        return cleaned;
    }

    public void register(String username, String email, String password) {
        registerWithRole(username, email, password, "USER");
    }

    public void registerAdmin(String username, String email, String password) {
        registerWithRole(username, email, password, "ADMIN");
    }

    private void registerWithRole(String username, String email, String password, String role) {
        User user = new User();
        user.setUsername(username.trim());
        user.setEmail(email.trim());
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setEnabled(true);
        user.setCreatedAt(Instant.now());
        userRepository.save(user);
    }
}
