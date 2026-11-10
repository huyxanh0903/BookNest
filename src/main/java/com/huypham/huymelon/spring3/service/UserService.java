package com.huypham.huymelon.spring3.service;

import com.huypham.huymelon.spring3.entity.User;
import com.huypham.huymelon.spring3.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

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

    public User getCurrentUser(Object principal) {
        if (principal instanceof UserDetails ud) {
            return findByUsername(ud.getUsername());
        }
        if (principal instanceof OAuth2User oauth2) {
            String email = oauth2.getAttribute("email");
            if (email != null && !email.isBlank()) {
                Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email.trim());
                if (userOpt.isPresent()) return userOpt.get();
            }
            String name = oauth2.getName();
            if (name != null && !name.isBlank()) {
                Optional<User> userOpt = userRepository.findByUsernameIgnoreCase(name.trim());
                if (userOpt.isPresent()) return userOpt.get();
            }
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String name = auth.getName();
            if (name != null && !name.isBlank()) {
                return userRepository.findByUsernameIgnoreCase(name)
                        .or(() -> userRepository.findByEmailIgnoreCase(name))
                        .orElseThrow(() -> new IllegalStateException("Authenticated account not found: " + name));
            }
        }
        throw new IllegalStateException("No authenticated user found.");
    }

    public User processOAuthUser(String email, String name) {
        Optional<User> existing = userRepository.findByEmailIgnoreCase(email.trim());
        if (existing.isPresent()) {
            return existing.get();
        }

        String baseUsername = generateBaseUsername(email, name);
        String username = baseUsername;
        int counter = 1;
        while (userRepository.existsByUsernameIgnoreCase(username)) {
            username = baseUsername + counter++;
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email.trim());
        newUser.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        newUser.setRole("USER");
        newUser.setEnabled(true);
        newUser.setCreatedAt(Instant.now());
        return userRepository.save(newUser);
    }

    private String generateBaseUsername(String email, String name) {
        if (name != null && !name.isBlank()) {
            String cleaned = name.trim().replaceAll("[^a-zA-Z0-9_]", "").toLowerCase();
            if (cleaned.length() >= 3) {
                return cleaned.length() > 40 ? cleaned.substring(0, 40) : cleaned;
            }
        }
        String prefix = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "").toLowerCase();
        if (prefix.length() < 3) prefix = "user_" + prefix;
        return prefix.length() > 40 ? prefix.substring(0, 40) : prefix;
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
