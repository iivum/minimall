package com.minimall.repository;

import com.minimall.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    List<Category> findByActiveTrueOrderBySortOrderAsc();
    List<Category> findByParentIsNullAndActiveTrueOrderBySortOrderAsc();
    List<Category> findByParentIdAndActiveTrueOrderBySortOrderAsc(String parentId);
    Page<Category> findByActiveTrue(Pageable pageable);
}