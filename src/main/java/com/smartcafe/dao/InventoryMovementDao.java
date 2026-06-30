package com.smartcafe.dao;

import com.smartcafe.model.InventoryMovement;
import java.util.List;

public interface InventoryMovementDao {
    void record(InventoryMovement movement);
    List<InventoryMovement> findByInventoryId(int inventoryId);
    List<InventoryMovement> findRecent(int limit);
}
