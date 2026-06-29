package com.smartcafe.service;

import com.smartcafe.model.InventoryItem;

import java.util.List;
import java.util.Optional;

public interface InventoryService {
    List<InventoryItem>    findAll();
    List<InventoryItem>    findLowStock();
    Optional<InventoryItem> findById(int id);
    long                   countLowStock();
    InventoryItem          create(String name, String unit, double currentStock,
                                  double minStock, Double costPerUnit, Integer supplierId);
    InventoryItem          update(InventoryItem item);
    void                   delete(int id);
}
