package com.huypham.huymelon.spring3.service;

import com.huypham.huymelon.spring3.entity.Category;
import com.huypham.huymelon.spring3.repository.CategoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {
    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<Category> findAll() { return repository.findAll(Sort.by("name").ascending()); }

    public Category create(String name) {
        String cleaned = normalize(name);
        if (repository.existsByNameIgnoreCase(cleaned)) throw new IllegalArgumentException("A category with this name already exists.");
        Category category = new Category();
        category.setName(cleaned);
        return repository.save(category);
    }

    public Category update(Integer id, String name) {
        Category category = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Category not found."));
        String cleaned = normalize(name);
        if (repository.findByNameIgnoreCase(cleaned).filter(existing -> !existing.getId().equals(id)).isPresent())
            throw new IllegalArgumentException("A category with this name already exists.");
        category.setName(cleaned);
        return repository.save(category);
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) throw new IllegalArgumentException("Category not found.");
        repository.deleteById(id);
    }

    private String normalize(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Category name is required.");
        String cleaned = name.trim();
        if (cleaned.length() > 100) throw new IllegalArgumentException("Category names must be 100 characters or fewer.");
        return cleaned;
    }
}
