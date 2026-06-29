package com.smartcafe.dao;

import com.smartcafe.model.Product;

import java.util.List;
import java.util.Optional;

/** Data-access contract for the {@code menu_items} table. */
public interface ProductDao {

    /** All active products with category name via JOIN, ordered by category then name. */
    List<Product> findAll();

    /** Filter by category. */
    List<Product> findByCategory(int categoryId);

    /** Case-insensitive search across name and description. */
    List<Product> search(String query);

    /** Search within a specific category (null categoryId = all categories). */
    List<Product> searchInCategory(String query, Integer categoryId);

    Optional<Product> findById(int id);

    boolean existsByName(String name);

    boolean existsByNameExcludingId(String name, int excludeId);

    /** Inserts and populates {@code product.id}. */
    Product save(Product product);

    void update(Product product);

    /** Soft-delete: sets {@code is_active = FALSE}. */
    void delete(int id);

    /** Count of all active products. */
    long count();

    /** Count of active products in a given category — used before category deletion. */
    long countActiveByCategory(int categoryId);
}
