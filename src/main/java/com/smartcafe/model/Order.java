package com.smartcafe.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {

    // Status constants (mirror DB ENUM)
    public static final String STATUS_NEW         = "NEW";
    public static final String STATUS_PENDING     = "PENDING";
    public static final String STATUS_CONFIRMED   = "CONFIRMED";
    public static final String STATUS_PREPARING   = "PREPARING";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_READY       = "READY";
    public static final String STATUS_SERVED      = "SERVED";
    public static final String STATUS_COMPLETED   = "COMPLETED";
    public static final String STATUS_CANCELLED   = "CANCELLED";

    public static final String[] ALL_ACTIVE_STATUSES = {
        STATUS_NEW, STATUS_PENDING, STATUS_CONFIRMED, STATUS_PREPARING, STATUS_IN_PROGRESS, STATUS_READY, STATUS_SERVED
    };

    public static String statusLabel(String status) {
        return switch (status) {
            case STATUS_NEW       -> "🆕 New";
            case STATUS_PENDING   -> "⏳ Pending";
            case STATUS_CONFIRMED -> "✅ Confirmed";
            case STATUS_PREPARING, STATUS_IN_PROGRESS -> "👨‍🍳 Preparing";
            case STATUS_READY     -> "🔔 Ready";
            case STATUS_SERVED    -> "🍽 Served";
            case STATUS_COMPLETED -> "✔ Completed";
            case STATUS_CANCELLED -> "❌ Cancelled";
            default               -> status;
        };
    }

    public static java.awt.Color statusColor(String status) {
        return switch (status) {
            case STATUS_NEW       -> new java.awt.Color(0x6C757D);
            case STATUS_PENDING   -> new java.awt.Color(0xFFC107);
            case STATUS_CONFIRMED -> new java.awt.Color(0x17A2B8);
            case STATUS_PREPARING, STATUS_IN_PROGRESS -> new java.awt.Color(0xFD7E14);
            case STATUS_READY     -> new java.awt.Color(0x28A745);
            case STATUS_SERVED    -> new java.awt.Color(0x20C997);
            case STATUS_COMPLETED -> new java.awt.Color(0x6610F2);
            case STATUS_CANCELLED -> new java.awt.Color(0xDC3545);
            default               -> java.awt.Color.GRAY;
        };
    }

    // Type constants
    public static final String TYPE_DINE_IN  = "DINE_IN";
    public static final String TYPE_TAKEAWAY = "TAKEAWAY";
    public static final String TYPE_DELIVERY = "DELIVERY";

    private int           id;
    private String        orderNumber;
    private Integer       tableId;
    private int           cashierId;
    private String        customerName;
    private String        orderType;
    private String        status;
    private double        subtotal;
    private double        tax;
    private double        discount;
    private double        total;
    private String        notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Transient — populated by JOIN queries or service layer
    private String        tableNumber;
    private String        cashierName;
    private List<OrderItem> items   = new ArrayList<>();
    private Payment         payment;

    public Order() {
        this.status    = STATUS_PENDING;
        this.orderType = TYPE_DINE_IN;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public int     getId()        { return id; }
    public void    setId(int v)   { id = v; }

    public String  getOrderNumber()          { return orderNumber; }
    public void    setOrderNumber(String v)  { orderNumber = v; }

    public Integer getTableId()          { return tableId; }
    public void    setTableId(Integer v) { tableId = v; }

    public int     getCashierId()       { return cashierId; }
    public void    setCashierId(int v)  { cashierId = v; }

    public String  getCustomerName()          { return customerName; }
    public void    setCustomerName(String v)  { customerName = v; }

    public String  getOrderType()          { return orderType; }
    public void    setOrderType(String v)  { orderType = v; }

    public String  getStatus()          { return status; }
    public void    setStatus(String v)  { status = v; }

    public double  getSubtotal()       { return subtotal; }
    public void    setSubtotal(double v) { subtotal = v; }

    public double  getTax()       { return tax; }
    public void    setTax(double v) { tax = v; }

    public double  getDiscount()       { return discount; }
    public void    setDiscount(double v) { discount = v; }

    public double  getTotal()       { return total; }
    public void    setTotal(double v) { total = v; }

    public String  getNotes()          { return notes; }
    public void    setNotes(String v)  { notes = v; }

    public LocalDateTime getCreatedAt()               { return createdAt; }
    public void          setCreatedAt(LocalDateTime v) { createdAt = v; }

    public LocalDateTime getUpdatedAt()               { return updatedAt; }
    public void          setUpdatedAt(LocalDateTime v) { updatedAt = v; }

    public String  getTableNumber()          { return tableNumber; }
    public void    setTableNumber(String v)  { tableNumber = v; }

    public String  getCashierName()          { return cashierName; }
    public void    setCashierName(String v)  { cashierName = v; }

    public List<OrderItem> getItems()              { return items; }
    public void            setItems(List<OrderItem> v) { items = v; }

    public Payment getPayment()           { return payment; }
    public void    setPayment(Payment v)  { payment = v; }
}
