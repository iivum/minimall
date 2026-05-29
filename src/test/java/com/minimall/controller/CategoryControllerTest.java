package com.minimall.controller;

import com.minimall.dto.CategoryDTO;
import com.minimall.model.Category;
import com.minimall.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    private Category createCategory(String id, String name, int sortOrder) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setSortOrder(sortOrder);
        category.setActive(true);
        return category;
    }

    @Test
    @WithMockUser
    void list_returnsPaginatedCategories() throws Exception {
        Category category = createCategory("cat-1", "Electronics", 1);
        Page<Category> page = new PageImpl<>(List.of(category));
        when(categoryRepository.findByActiveTrue(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/admin/categories")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("Electronics"));
    }

    @Test
    @WithMockUser
    void listAll_returnsAllCategories() throws Exception {
        Category cat1 = createCategory("cat-1", "Electronics", 1);
        Category cat2 = createCategory("cat-2", "Clothing", 2);
        when(categoryRepository.findByActiveTrueOrderBySortOrderAsc()).thenReturn(List.of(cat1, cat2));

        mockMvc.perform(get("/api/admin/categories/all"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("Electronics"))
            .andExpect(jsonPath("$[1].name").value("Clothing"));
    }

    @Test
    @WithMockUser
    void getById_existingCategory_returnsCategory() throws Exception {
        Category category = createCategory("cat-1", "Electronics", 1);
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(category));

        mockMvc.perform(get("/api/admin/categories/cat-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    @WithMockUser
    void getById_nonexistentCategory_returnsNotFound() throws Exception {
        when(categoryRepository.findById("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/admin/categories/nonexistent"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void create_withValidRequest_returnsCreatedCategory() throws Exception {
        Category category = createCategory("cat-new", "New Category", 5);
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
    void create_withBlankName_returnsBadRequest() throws Exception {
        String requestBody = """
            {
                "name": ""
            }
            """;

        mockMvc.perform(post("/api/admin/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void update_existingCategory_returnsUpdatedCategory() throws Exception {
        Category category = createCategory("cat-1", "Updated Name", 3);
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        String requestBody = """
            {
                "name": "Updated Name",
                "sortOrder": 3
            }
            """;

        mockMvc.perform(put("/api/admin/categories/cat-1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @WithMockUser
    void update_nonexistentCategory_returnsNotFound() throws Exception {
        when(categoryRepository.findById("nonexistent")).thenReturn(Optional.empty());

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
        Category category = createCategory("cat-1", "Electronics", 1);
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(category));

        mockMvc.perform(delete("/api/admin/categories/cat-1")
                .with(csrf()))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void delete_nonexistentCategory_returnsNotFound() throws Exception {
        when(categoryRepository.findById("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/admin/categories/nonexistent")
                .with(csrf()))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void create_withParentId_resolvesParentCategory() throws Exception {
        Category parent = createCategory("parent-cat", "Parent", 1);
        Category child = createCategory("child-cat", "Child", 2);
        child.setParent(parent);

        when(categoryRepository.findById("parent-cat")).thenReturn(Optional.of(parent));
        when(categoryRepository.save(any(Category.class))).thenReturn(child);

        String requestBody = """
            {
                "name": "Child",
                "parentId": "parent-cat"
            }
            """;

        mockMvc.perform(post("/api/admin/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk());
    }
}