package com.minimall.controller;

import com.minimall.model.Category;
import com.minimall.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import(TestSecurityConfig.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryRepository categoryRepository;

    @Test
    @WithMockUser
    void list_returnsActiveCategories() throws Exception {
        Category cat1 = new Category();
        cat1.setId("cat-1");
        cat1.setName("Electronics");
        cat1.setSortOrder(1);
        cat1.setActive(true);

        Category cat2 = new Category();
        cat2.setId("cat-2");
        cat2.setName("Clothing");
        cat2.setSortOrder(2);
        cat2.setActive(true);

        when(categoryRepository.findByActiveTrueOrderBySortOrderAsc()).thenReturn(List.of(cat1, cat2));

        mockMvc.perform(get("/api/admin/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("cat-1"))
            .andExpect(jsonPath("$[0].name").value("Electronics"))
            .andExpect(jsonPath("$[1].id").value("cat-2"))
            .andExpect(jsonPath("$[1].name").value("Clothing"));
    }

    @Test
    @WithMockUser
    void getById_existingCategory_returnsCategory() throws Exception {
        Category cat = new Category();
        cat.setId("cat-1");
        cat.setName("Electronics");
        cat.setSortOrder(1);
        cat.setActive(true);

        when(categoryRepository.findById("cat-1")).thenReturn(java.util.Optional.of(cat));

        mockMvc.perform(get("/api/admin/categories/cat-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("cat-1"))
            .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    @WithMockUser
    void getById_nonExisting_returnsNotFound() throws Exception {
        when(categoryRepository.findById("nonexistent")).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/admin/categories/nonexistent"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void create_withValidRequest_returnsCreatedCategory() throws Exception {
        Category saved = new Category();
        saved.setId("new-cat");
        saved.setName("New Category");
        saved.setSortOrder(5);
        saved.setActive(true);

        when(categoryRepository.save(any(Category.class))).thenReturn(saved);
        when(categoryRepository.findById(any(String.class))).thenReturn(java.util.Optional.empty());

        String requestBody = """
            {
                "name": "New Category",
                "sortOrder": 5
            }
            """;

        mockMvc.perform(post("/api/admin/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("new-cat"))
            .andExpect(jsonPath("$.name").value("New Category"))
            .andExpect(jsonPath("$.sortOrder").value(5))
            .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @WithMockUser
    void create_withParentId_returnsCategoryWithParent() throws Exception {
        Category parent = new Category();
        parent.setId("parent-cat");

        Category saved = new Category();
        saved.setId("child-cat");
        saved.setName("Child Category");
        saved.setSortOrder(1);
        saved.setActive(true);
        saved.setParent(parent);

        when(categoryRepository.findById("parent-cat")).thenReturn(java.util.Optional.of(parent));
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        String requestBody = """
            {
                "name": "Child Category",
                "sortOrder": 1,
                "parentId": "parent-cat"
            }
            """;

        mockMvc.perform(post("/api/admin/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("child-cat"))
            .andExpect(jsonPath("$.name").value("Child Category"));
    }

    @Test
    @WithMockUser
    void update_existingCategory_returnsUpdatedCategory() throws Exception {
        Category existing = new Category();
        existing.setId("cat-1");
        existing.setName("Old Name");
        existing.setSortOrder(1);
        existing.setActive(true);

        Category updated = new Category();
        updated.setId("cat-1");
        updated.setName("Updated Name");
        updated.setSortOrder(2);
        updated.setActive(true);

        when(categoryRepository.findById("cat-1")).thenReturn(java.util.Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenReturn(updated);

        String requestBody = """
            {
                "name": "Updated Name",
                "sortOrder": 2
            }
            """;

        mockMvc.perform(put("/api/admin/categories/cat-1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Name"))
            .andExpect(jsonPath("$.sortOrder").value(2));
    }

    @Test
    @WithMockUser
    void update_nonExisting_returnsNotFound() throws Exception {
        when(categoryRepository.findById("nonexistent")).thenReturn(java.util.Optional.empty());

        String requestBody = """
            {
                "name": "Updated Name"
            }
            """;

        mockMvc.perform(put("/api/admin/categories/nonexistent")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void delete_existingCategory_returnsOk() throws Exception {
        Category existing = new Category();
        existing.setId("cat-1");
        existing.setName("To Delete");
        existing.setActive(true);

        when(categoryRepository.findById("cat-1")).thenReturn(java.util.Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenReturn(existing);

        mockMvc.perform(delete("/api/admin/categories/cat-1")
                .with(csrf()))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void delete_nonExisting_returnsNotFound() throws Exception {
        when(categoryRepository.findById("nonexistent")).thenReturn(java.util.Optional.empty());

        mockMvc.perform(delete("/api/admin/categories/nonexistent")
                .with(csrf()))
            .andExpect(status().isNotFound());
    }
}