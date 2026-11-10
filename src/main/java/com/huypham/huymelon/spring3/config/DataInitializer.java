package com.huypham.huymelon.spring3.config;

import com.huypham.huymelon.spring3.entity.Category;
import com.huypham.huymelon.spring3.repository.CategoryRepository;
import com.huypham.huymelon.spring3.repository.UserRepository;
import com.huypham.huymelon.spring3.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final UserService userService;
    private final CategoryRepository categoryRepository;

    public DataInitializer(UserRepository userRepository, UserService userService, CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        // Automatically create a default admin account if not already present
        if (!userRepository.existsByUsernameIgnoreCase("admin")) {
            userService.registerAdmin("admin", "admin@booknest.local", "admin123");
            log.info("Initialized default administrator account: username='admin', password='admin123'");
        }

        // Seed initial categories if catalog is completely empty
        if (categoryRepository.count() == 0) {
            List<String> defaults = List.of("Literature & Fiction", "Science & Technology", "History", "Philosophy", "Art & Design");
            for (String name : defaults) {
                Category cat = new Category();
                cat.setName(name);
                categoryRepository.save(cat);
            }
            log.info("Initialized default catalog categories.");
        }
    }
}

