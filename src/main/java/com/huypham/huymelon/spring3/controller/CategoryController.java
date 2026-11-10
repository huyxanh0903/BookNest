package com.huypham.huymelon.spring3.controller;

import com.huypham.huymelon.spring3.service.CategoryService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
@PreAuthorize("hasRole('ADMIN')")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) { this.categoryService = categoryService; }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "categories";
    }

    @PostMapping
    public String create(@RequestParam String name, RedirectAttributes redirect) {
        try { categoryService.create(name); redirect.addFlashAttribute("notice", "Category added."); }
        catch (IllegalArgumentException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/categories";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable Integer id, @RequestParam String name, RedirectAttributes redirect) {
        try { categoryService.update(id, name); redirect.addFlashAttribute("notice", "Category updated."); }
        catch (IllegalArgumentException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
        try { categoryService.delete(id); redirect.addFlashAttribute("notice", "Category deleted."); }
        catch (IllegalArgumentException | DataIntegrityViolationException ex) {
            redirect.addFlashAttribute("error", ex instanceof DataIntegrityViolationException
                    ? "This category is assigned to one or more books and cannot be deleted." : ex.getMessage());
        }
        return "redirect:/categories";
    }
}
