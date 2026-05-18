package com.minimall.controller;

import com.minimall.model.Category;
import com.minimall.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryRepository categoryRepository;

    @MockBean
    private com.minimall.service.JwtService jwtService;

    @Test
    @WithMockUser
    void list_returnsActiveCategories() throws Exception {
        Category category = new Category();
        category.setId("cat-1");
        category.setName("Electronics");
        category.setSortOrder(1);
        category.setActive(true);

        when(categoryRepository.findByActiveTrueOrderBySortOrderAsc()).thenReturn(List.of(category));

        mockMvc.perform(get("/api/admin/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Electronics"));
    }

    @Test
    @WithMockUser
    void getById_returnsCategory() throws Exception {
        Category category = new Category();
        category.setId("cat-1");
        category.setName("Electronics");
        category.setSortOrder(1);
        category.setActive(true);

        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(category));

        mockMvc.perform(get("/api/admin/categories/cat-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    @WithMockUser
    void getById_returns404WhenNotFound() throws Exception {
        when(categoryRepository.findById("cat-99")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/admin/categories/cat-99"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void create_returnsCreatedCategory() throws Exception {
        Category category = new Category();
        category.setId("cat-new");
        category.setName("New Category");
        category.setSortOrder(5);
        category.setActive(true);

        when(categoryRepository.save(any(Category.class))).thenReturn(category);

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
            .andExpect(jsonPath("$.name").value("New Category"));
    }

    @Test
    @WithMockUser
    void create_withParentId_returnsCategoryWithParent() throws Exception {
        Category parent = new Category();
        parent.setId("parent-1");
        parent.setName("Parent");

        Category category = new Category();
        category.setId("cat-new");
        category.setName("Child Category");
        category.setSortOrder(1);
        category.setActive(true);
        category.setParent(parent);

        when(categoryRepository.findById("parent-1")).thenReturn(Optional.of(parent));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        String requestBody = """
            {
                "name": "Child Category",
                "parentId": "parent-1"
            }
            """;

        mockMvc.perform(post("/api/admin/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.parent.id").value("parent-1"));
    }

    @Test
    @WithMockUser
    void update_returnsUpdatedCategory() throws Exception {
        Category category = new Category();
        category.setId("cat-1");
        category.setName("Updated Category");
        category.setSortOrder(10);
        category.setActive(true);

        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        String requestBody = """
            {
                "name": "Updated Category",
                "sortOrder": 10
            }
            """;

        mockMvc.perform(put("/api/admin/categories/cat-1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Category"))
            .andExpect(jsonPath("$.sortOrder").value(10));
    }

    @Test
    @WithMockUser
    void update_returns404WhenNotFound() throws Exception {
        when(categoryRepository.findById("cat-99")).thenReturn(Optional.empty());

        String requestBody = """
            {
                "name": "Updated"
            }
            """;

        mockMvc.perform(put("/api/admin/categories/cat-99")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void delete_softDeletesCategory() throws Exception {
        Category category = new Category();
        category.setId("cat-1");
        category.setName("Electronics");
        category.setActive(true);

        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(category));

        mockMvc.perform(delete("/api/admin/categories/cat-1")
                .with(csrf()))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void delete_returns404WhenNotFound() throws Exception {
        when(categoryRepository.findById("cat-99")).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/admin/categories/cat-99")
                .with(csrf()))
            .andExpect(status().isNotFound());
    }
}