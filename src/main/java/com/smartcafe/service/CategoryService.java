package com.smartcafe.service;

import com.smartcafe.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    List<Category> findAll();

    List<Category> search(String query);

    Optional<Category> findById(int id);

    /**
     * Validates and creates a new category.
     * @throws com.smartcafe.exception.ValidationException on blank name or duplicate
     */
    Category create(String name, String description, int sortOrder);

    /**
     * Validates and persists changes.
     * @throws com.smartcafe.exception.ValidationException on blank name or duplicate
     */
    Category update(Category category);

    /**
     * Soft-deletes the category.
     * @throws com.smartcafe.exception.ValidationException if the category has active products
     */
    void delete(int id);
}
