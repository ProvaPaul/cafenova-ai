package com.smartcafe.dao;

import com.smartcafe.model.Category;

import java.util.List;
import java.util.Optional;

/** Data-access contract for the {@code categories} table. */
public interface CategoryDao {

    /** All active categories ordered by sort_order ASC, name ASC. */
    List<Category> findAll();

    /** Filter by name or description (case-insensitive LIKE). */
    List<Category> search(String query);

    Optional<Category> findById(int id);

    boolean existsByName(String name);

    /** Same check but excludes the given ID — used during updates. */
    boolean existsByNameExcludingId(String name, int excludeId);

    /** Inserts the record and populates {@code category.id}. */
    Category save(Category category);

    void update(Category category);

    /** Soft-delete: sets {@code is_active = FALSE}. */
    void delete(int id);

    /** Total active category count. */
    long count();
}
