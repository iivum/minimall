package com.minimall.dto;

import com.minimall.model.Category;

public class CategoryDTO {
    private String id;
    private String name;
    private Integer sortOrder;
    private Boolean active;
    private String parentId;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public static CategoryDTO from(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setSortOrder(category.getSortOrder());
        dto.setActive(category.getActive());
        dto.setParentId(category.getParent() != null ? category.getParent().getId() : null);
        return dto;
    }
}