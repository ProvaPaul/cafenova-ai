package com.smartcafe.dao;

import com.smartcafe.model.InventoryItem;

import java.util.List;
import java.util.Optional;

public interface InventoryItemDao {
    List<InventoryItem>    findAll();                         // active only, with supplier JOIN
    List<InventoryItem>    findLowStock();                    // current_stock <= min_stock
    Optional<InventoryItem> findById(int id);
    boolean                existsByName(String name);
    boolean                existsByNameExcludingId(String name, int excludeId);
    InventoryItem          save(InventoryItem item);
    void                   update(InventoryItem item);
    void                   delete(int id);                   // soft delete
    long                   countLowStock();
}
