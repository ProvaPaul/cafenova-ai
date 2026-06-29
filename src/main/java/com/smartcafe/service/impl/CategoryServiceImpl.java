package com.smartcafe.service.impl;

import com.smartcafe.dao.CategoryDao;
import com.smartcafe.dao.ProductDao;
import com.smartcafe.exception.ValidationException;
import com.smartcafe.model.Category;
import com.smartcafe.service.CategoryService;

import java.util.List;
import java.util.Optional;

public class CategoryServiceImpl implements CategoryService {

    private final CategoryDao categoryDao;
    private final ProductDao  productDao;

    public CategoryServiceImpl(CategoryDao categoryDao, ProductDao productDao) {
        this.categoryDao = categoryDao;
        this.productDao  = productDao;
    }

    @Override
    public List<Category> findAll() {
        return categoryDao.findAll();
    }

    @Override
    public List<Category> search(String query) {
        if (query == null || query.isBlank()) return categoryDao.findAll();
        return categoryDao.search(query.trim());
    }

    @Override
    public Optional<Category> findById(int id) {
        return categoryDao.findById(id);
    }

    @Override
    public Category create(String name, String description, int sortOrder) {
        name = requireName(name);

        if (categoryDao.existsByName(name)) {
            throw new ValidationException("A category named '" + name + "' already exists");
        }

        return categoryDao.save(new Category(name,
                description != null ? description.trim() : null,
                sortOrder));
    }

    @Override
    public Category update(Category category) {
        requireName(category.getName());

        if (categoryDao.existsByNameExcludingId(category.getName().trim(), category.getId())) {
            throw new ValidationException("A category named '" + category.getName() + "' already exists");
        }

        category.setName(category.getName().trim());
        categoryDao.update(category);
        return category;
    }

    @Override
    public void delete(int id) {
        Category cat = categoryDao.findById(id)
                .orElseThrow(() -> new ValidationException("Category not found"));

        long productCount = productDao.countActiveByCategory(id);
        if (productCount > 0) {
            throw new ValidationException(
                "Cannot delete '" + cat.getName() + "': it has " + productCount +
                " active product(s). Please delete or reassign those products first.");
        }

        categoryDao.delete(id);
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) throw new ValidationException("Category name is required");
        return name.trim();
    }
}
