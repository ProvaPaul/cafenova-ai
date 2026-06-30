package com.smartcafe.service;

import com.smartcafe.model.InventoryMovement;
import java.util.List;

public interface InventoryMovementService {
    void recordStockIn(int inventoryId, double qty, String notes, Integer userId);
    void recordAdjustment(int inventoryId, double newQty, String notes, Integer userId);
    List<InventoryMovement> getHistory(int inventoryId);
    List<InventoryMovement> getRecent(int limit);
}
