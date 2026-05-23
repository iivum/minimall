package com.minimall.controller;

import com.minimall.model.Category;
import com.minimall.repository.CategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@Tag(name = "Category", description = "Category Management APIs")
public class CategoryController {
    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    @Operation(summary = "List all active categories")
    public ResponseEntity<List<Category>> list() {
        return ResponseEntity.ok(categoryRepository.findByActiveTrueOrderBySortOrderAsc());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by id")
    public ResponseEntity<Category> getById(@PathVariable String id) {
        return categoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new category")
    public ResponseEntity<Category> create(@Valid @RequestBody CategoryRequest request) {
        Category category = new Category();
        category.setName(request.name());
        category.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
        category.setActive(true);
        if (request.parentId() != null) {
            categoryRepository.findById(request.parentId()).ifPresent(category::setParent);
        }
        return ResponseEntity.ok(categoryRepository.save(category));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category")
    public ResponseEntity<Category> update(@PathVariable String id, @Valid @RequestBody CategoryRequest request) {
        return categoryRepository.findById(id).map(category -> {
            category.setName(request.name());
            if (request.sortOrder() != null) {
                category.setSortOrder(request.sortOrder());
            }
            if (request.active() != null) {
                category.setActive(request.active());
            }
            if (request.parentId() != null) {
                categoryRepository.findById(request.parentId()).ifPresent(category::setParent);
            }
            return ResponseEntity.ok(categoryRepository.save(category));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete category")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        return categoryRepository.findById(id).map(category -> {
            category.setActive(false);
            categoryRepository.save(category);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    public record CategoryRequest(@NotBlank String name, Integer sortOrder, Boolean active, String parentId) {}
}