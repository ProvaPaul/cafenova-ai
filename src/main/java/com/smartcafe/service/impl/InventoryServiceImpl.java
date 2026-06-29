package com.smartcafe.service.impl;

import com.smartcafe.dao.InventoryItemDao;
import com.smartcafe.exception.ValidationException;
import com.smartcafe.model.InventoryItem;
import com.smartcafe.service.InventoryService;

import java.util.List;
import java.util.Optional;

public class InventoryServiceImpl implements InventoryService {

    private final InventoryItemDao inventoryDao;

    public InventoryServiceImpl(InventoryItemDao inventoryDao) {
        this.inventoryDao = inventoryDao;
    }

    @Override
    public List<InventoryItem> findAll() { return inventoryDao.findAll(); }

    @Override
    public List<InventoryItem> findLowStock() { return inventoryDao.findLowStock(); }

    @Override
    public Optional<InventoryItem> findById(int id) { return inventoryDao.findById(id); }

    @Override
    public long countLowStock() { return inventoryDao.countLowStock(); }

    @Override
    public InventoryItem create(String name, String unit, double currentStock,
                                double minStock, Double costPerUnit, Integer supplierId) {
        name = requireName(name);
        requireUnit(unit);
        if (currentStock < 0) throw new ValidationException("Current stock cannot be negative");
        if (minStock < 0)     throw new ValidationException("Minimum stock cannot be negative");

        if (inventoryDao.existsByName(name))
            throw new ValidationException("An item named '" + name + "' already exists");

        InventoryItem item = new InventoryItem();
        item.setName(name);
        item.setUnit(unit.trim());
        item.setCurrentStock(currentStock);
        item.setMinStock(minStock);
        item.setCostPerUnit(costPerUnit);
        item.setSupplierId(supplierId);
        return inventoryDao.save(item);
    }

    @Override
    public InventoryItem update(InventoryItem item) {
        requireName(item.getName());
        requireUnit(item.getUnit());
        if (item.getCurrentStock() < 0) throw new ValidationException("Current stock cannot be negative");
        if (item.getMinStock() < 0)     throw new ValidationException("Minimum stock cannot be negative");

        if (inventoryDao.existsByNameExcludingId(item.getName().trim(), item.getId()))
            throw new ValidationException("An item named '" + item.getName() + "' already exists");

        item.setName(item.getName().trim());
        inventoryDao.update(item);
        return item;
    }

    @Override
    public void delete(int id) {
        inventoryDao.findById(id)
                .orElseThrow(() -> new ValidationException("Inventory item not found"));
        inventoryDao.delete(id);
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank())
            throw new ValidationException("Item name is required");
        return name.trim();
    }

    private static void requireUnit(String unit) {
        if (unit == null || unit.isBlank())
            throw new ValidationException("Unit is required (e.g. kg, litre, piece)");
    }
}
