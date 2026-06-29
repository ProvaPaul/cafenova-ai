package com.smartcafe.model;

import java.time.LocalDateTime;

public class InventoryItem {

    private int           id;
    private String        name;
    private String        unit;
    private double        currentStock;
    private double        minStock;
    private Double        costPerUnit;
    private Integer       supplierId;
    private boolean       active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Transient — populated by JOIN in DAO
    private String supplierName;

    public InventoryItem() { this.active = true; }

    // ── Computed ──────────────────────────────────────────────────────────────

    public boolean isLowStock() {
        return active && currentStock <= minStock;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public int     getId()        { return id; }
    public void    setId(int v)   { id = v; }

    public String  getName()          { return name; }
    public void    setName(String v)  { name = v; }

    public String  getUnit()          { return unit; }
    public void    setUnit(String v)  { unit = v; }

    public double  getCurrentStock()       { return currentStock; }
    public void    setCurrentStock(double v) { currentStock = v; }

    public double  getMinStock()       { return minStock; }
    public void    setMinStock(double v) { minStock = v; }

    public Double  getCostPerUnit()       { return costPerUnit; }
    public void    setCostPerUnit(Double v) { costPerUnit = v; }

    public Integer getSupplierId()        { return supplierId; }
    public void    setSupplierId(Integer v) { supplierId = v; }

    public boolean isActive()           { return active; }
    public void    setActive(boolean v) { active = v; }

    public LocalDateTime getCreatedAt()               { return createdAt; }
    public void          setCreatedAt(LocalDateTime v) { createdAt = v; }

    public LocalDateTime getUpdatedAt()               { return updatedAt; }
    public void          setUpdatedAt(LocalDateTime v) { updatedAt = v; }

    public String getSupplierName()           { return supplierName; }
    public void   setSupplierName(String v)   { supplierName = v; }
}
