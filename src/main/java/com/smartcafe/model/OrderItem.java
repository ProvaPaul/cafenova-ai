package com.smartcafe.model;

import java.time.LocalDateTime;

public class OrderItem {

    private int           id;
    private int           orderId;
    private int           menuItemId;
    private int           quantity;
    private double        unitPrice;
    private double        subtotal;
    private String        notes;
    private String        status;
    private LocalDateTime createdAt;

    // Transient — populated by JOIN
    private String productName;

    public OrderItem() { this.status = "PENDING"; }

    public OrderItem(int menuItemId, String productName, int quantity, double unitPrice) {
        this();
        this.menuItemId  = menuItemId;
        this.productName = productName;
        this.quantity    = quantity;
        this.unitPrice   = unitPrice;
        this.subtotal    = unitPrice * quantity;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public int     getId()        { return id; }
    public void    setId(int v)   { id = v; }

    public int     getOrderId()       { return orderId; }
    public void    setOrderId(int v)  { orderId = v; }

    public int     getMenuItemId()       { return menuItemId; }
    public void    setMenuItemId(int v)  { menuItemId = v; }

    public int     getQuantity()       { return quantity; }
    public void    setQuantity(int v)  { quantity = v; subtotal = unitPrice * v; }

    public double  getUnitPrice()       { return unitPrice; }
    public void    setUnitPrice(double v) { unitPrice = v; subtotal = v * quantity; }

    public double  getSubtotal()       { return subtotal; }
    public void    setSubtotal(double v) { subtotal = v; }

    public String  getNotes()          { return notes; }
    public void    setNotes(String v)  { notes = v; }

    public String  getStatus()          { return status; }
    public void    setStatus(String v)  { status = v; }

    public LocalDateTime getCreatedAt()               { return createdAt; }
    public void          setCreatedAt(LocalDateTime v) { createdAt = v; }

    public String  getProductName()          { return productName; }
    public void    setProductName(String v)  { productName = v; }
}
