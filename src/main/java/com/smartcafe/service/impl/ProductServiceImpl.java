package com.smartcafe.service.impl;

import com.smartcafe.dao.CategoryDao;
import com.smartcafe.dao.ProductDao;
import com.smartcafe.exception.ValidationException;
import com.smartcafe.model.Product;
import com.smartcafe.service.ProductService;

import java.util.List;
import java.util.Optional;

public class ProductServiceImpl implements ProductService {

    private final ProductDao  productDao;
    private final CategoryDao categoryDao;

    public ProductServiceImpl(ProductDao productDao, CategoryDao categoryDao) {
        this.productDao  = productDao;
        this.categoryDao = categoryDao;
    }

    @Override
    public List<Product> findAll() {
        return productDao.findAll();
    }

    @Override
    public List<Product> findByCategory(int categoryId) {
        return productDao.findByCategory(categoryId);
    }

    @Override
    public List<Product> search(String query, Integer categoryId) {
        boolean hasQuery    = query != null && !query.isBlank();
        boolean hasCategory = categoryId != null;

        if (!hasQuery && !hasCategory) return productDao.findAll();
        if (!hasQuery)                 return productDao.findByCategory(categoryId);
        return productDao.searchInCategory(query.trim(), categoryId);
    }

    @Override
    public Optional<Product> findById(int id) {
        return productDao.findById(id);
    }

    @Override
    public Product create(String name, String description, int categoryId,
                          double price, double costPrice, String imagePath, boolean available) {
        name = requireName(name);
        validateCategory(categoryId);
        validatePrice(price);

        if (productDao.existsByName(name)) {
            throw new ValidationException("A product named '" + name + "' already exists");
        }

        Product p = new Product();
        p.setName(name);
        p.setDescription(description != null ? description.trim() : null);
        p.setCategoryId(categoryId);
        p.setPrice(price);
        p.setCostPrice(costPrice);
        p.setImagePath(imagePath != null && !imagePath.isBlank() ? imagePath.trim() : null);
        p.setAvailable(available);
        p.setActive(true);
        return productDao.save(p);
    }

    @Override
    public Product update(Product product) {
        requireName(product.getName());
        validateCategory(product.getCategoryId());
        validatePrice(product.getPrice());

        if (productDao.existsByNameExcludingId(product.getName().trim(), product.getId())) {
            throw new ValidationException("A product named '" + product.getName() + "' already exists");
        }

        product.setName(product.getName().trim());
        productDao.update(product);
        return product;
    }

    @Override
    public void delete(int id) {
        productDao.findById(id)
                .orElseThrow(() -> new ValidationException("Product not found"));
        productDao.delete(id);
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) throw new ValidationException("Product name is required");
        return name.trim();
    }

    private void validateCategory(int categoryId) {
        categoryDao.findById(categoryId)
                .orElseThrow(() -> new ValidationException("Please select a valid category"));
    }

    private static void validatePrice(double price) {
        if (price <= 0) throw new ValidationException("Price must be greater than zero");
    }
}
