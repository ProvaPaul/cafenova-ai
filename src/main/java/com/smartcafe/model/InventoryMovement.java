package com.smartcafe.model;

import java.time.LocalDateTime;

public class InventoryMovement {

    public static final String TYPE_STOCK_IN        = "STOCK_IN";
    public static final String TYPE_STOCK_OUT       = "STOCK_OUT";
    public static final String TYPE_ADJUSTMENT      = "ADJUSTMENT";
    public static final String TYPE_ORDER_DEDUCTION = "ORDER_DEDUCTION";

    private int           id;
    private int           inventoryId;
    private String        inventoryName;
    private String        movementType;
    private double        quantity;
    private double        quantityBefore;
    private double        quantityAfter;
    private Integer       referenceId;
    private String        notes;
    private Integer       createdBy;
    private String        createdByName;
    private LocalDateTime createdAt;

    public InventoryMovement() {}

    public InventoryMovement(int inventoryId, String movementType, double quantity,
                              double quantityBefore, Integer referenceId, String notes, Integer createdBy) {
        this.inventoryId    = inventoryId;
        this.movementType   = movementType;
        this.quantity       = quantity;
        this.quantityBefore = quantityBefore;
        this.quantityAfter  = movementType.equals(TYPE_STOCK_IN)
                ? quantityBefore + quantity : quantityBefore - quantity;
        this.referenceId    = referenceId;
        this.notes          = notes;
        this.createdBy      = createdBy;
    }

    public int           getId()                       { return id; }
    public void          setId(int v)                  { id = v; }
    public int           getInventoryId()              { return inventoryId; }
    public void          setInventoryId(int v)         { inventoryId = v; }
    public String        getInventoryName()            { return inventoryName; }
    public void          setInventoryName(String v)    { inventoryName = v; }
    public String        getMovementType()             { return movementType; }
    public void          setMovementType(String v)     { movementType = v; }
    public double        getQuantity()                 { return quantity; }
    public void          setQuantity(double v)         { quantity = v; }
    public double        getQuantityBefore()           { return quantityBefore; }
    public void          setQuantityBefore(double v)   { quantityBefore = v; }
    public double        getQuantityAfter()            { return quantityAfter; }
    public void          setQuantityAfter(double v)    { quantityAfter = v; }
    public Integer       getReferenceId()              { return referenceId; }
    public void          setReferenceId(Integer v)     { referenceId = v; }
    public String        getNotes()                    { return notes; }
    public void          setNotes(String v)            { notes = v; }
    public Integer       getCreatedBy()                { return createdBy; }
    public void          setCreatedBy(Integer v)       { createdBy = v; }
    public String        getCreatedByName()            { return createdByName; }
    public void          setCreatedByName(String v)    { createdByName = v; }
    public LocalDateTime getCreatedAt()                { return createdAt; }
    public void          setCreatedAt(LocalDateTime v) { createdAt = v; }

    public String getTypeLabel() {
        return switch (movementType) {
            case TYPE_STOCK_IN        -> "Stock In";
            case TYPE_STOCK_OUT       -> "Stock Out";
            case TYPE_ADJUSTMENT      -> "Adjustment";
            case TYPE_ORDER_DEDUCTION -> "Order Deduction";
            default -> movementType;
        };
    }
}
