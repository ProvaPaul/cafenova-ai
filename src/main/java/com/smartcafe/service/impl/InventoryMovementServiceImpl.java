package com.smartcafe.service.impl;

import com.smartcafe.dao.InventoryItemDao;
import com.smartcafe.dao.InventoryMovementDao;
import com.smartcafe.exception.DatabaseException;
import com.smartcafe.model.InventoryItem;
import com.smartcafe.model.InventoryMovement;
import com.smartcafe.service.InventoryMovementService;

import java.util.List;

public class InventoryMovementServiceImpl implements InventoryMovementService {

    private final InventoryMovementDao movementDao;
    private final InventoryItemDao     itemDao;

    public InventoryMovementServiceImpl(InventoryMovementDao movementDao, InventoryItemDao itemDao) {
        this.movementDao = movementDao;
        this.itemDao     = itemDao;
    }

    @Override
    public void recordStockIn(int inventoryId, double qty, String notes, Integer userId) {
        InventoryItem item = itemDao.findById(inventoryId)
                .orElseThrow(() -> new DatabaseException("Inventory item not found", null));
        double before = item.getCurrentStock();
        double after  = before + qty;
        InventoryMovement m = new InventoryMovement();
        m.setInventoryId(inventoryId);
        m.setMovementType(InventoryMovement.TYPE_STOCK_IN);
        m.setQuantity(qty);
        m.setQuantityBefore(before);
        m.setQuantityAfter(after);
        m.setNotes(notes);
        m.setCreatedBy(userId);
        movementDao.record(m);
        item.setCurrentStock(after);
        itemDao.update(item);
    }

    @Override
    public void recordAdjustment(int inventoryId, double newQty, String notes, Integer userId) {
        InventoryItem item = itemDao.findById(inventoryId)
                .orElseThrow(() -> new DatabaseException("Inventory item not found", null));
        double before = item.getCurrentStock();
        double diff   = newQty - before;
        InventoryMovement m = new InventoryMovement();
        m.setInventoryId(inventoryId);
        m.setMovementType(InventoryMovement.TYPE_ADJUSTMENT);
        m.setQuantity(Math.abs(diff));
        m.setQuantityBefore(before);
        m.setQuantityAfter(newQty);
        m.setNotes(notes != null ? notes : "Manual adjustment");
        m.setCreatedBy(userId);
        movementDao.record(m);
        item.setCurrentStock(newQty);
        itemDao.update(item);
    }

    @Override public List<InventoryMovement> getHistory(int id) { return movementDao.findByInventoryId(id); }
    @Override public List<InventoryMovement> getRecent(int n)   { return movementDao.findRecent(n); }
}
