package com.smartcafe.service;

import com.smartcafe.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    List<Product> findAll();

    List<Product> findByCategory(int categoryId);

    /** Combined search + category filter. Pass null values to skip either filter. */
    List<Product> search(String query, Integer categoryId);

    Optional<Product> findById(int id);

    Product create(String name, String description, int categoryId,
                   double price, double costPrice, String imagePath, boolean available);

    Product update(Product product);

    /** Soft-delete. */
    void delete(int id);
}
