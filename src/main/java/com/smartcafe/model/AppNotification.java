package com.smartcafe.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppNotification {

    public static final String TYPE_NEW_ORDER        = "NEW_ORDER";
    public static final String TYPE_LOW_STOCK        = "LOW_STOCK";
    public static final String TYPE_ORDER_COMPLETED  = "ORDER_COMPLETED";
    public static final String TYPE_RESERVATION_TODAY= "RESERVATION_TODAY";
    public static final String TYPE_ORDER_STATUS     = "ORDER_STATUS";
    public static final String TYPE_PAYMENT_VOIDED   = "PAYMENT_VOIDED";

    private int           id;
    private String        type;
    private String        title;
    private String        message;
    private Integer       referenceId;
    private boolean       read;
    private LocalDateTime createdAt;

    public AppNotification() {}

    public AppNotification(String type, String title, String message, Integer referenceId) {
        this.type = type; this.title = title; this.message = message; this.referenceId = referenceId;
    }

    public int           getId()                       { return id; }
    public void          setId(int v)                  { id = v; }
    public String        getType()                     { return type; }
    public void          setType(String v)             { type = v; }
    public String        getTitle()                    { return title; }
    public void          setTitle(String v)            { title = v; }
    public String        getMessage()                  { return message; }
    public void          setMessage(String v)          { message = v; }
    public Integer       getReferenceId()              { return referenceId; }
    public void          setReferenceId(Integer v)     { referenceId = v; }
    public boolean       isRead()                      { return read; }
    public void          setRead(boolean v)            { read = v; }
    public LocalDateTime getCreatedAt()                { return createdAt; }
    public void          setCreatedAt(LocalDateTime v) { createdAt = v; }

    public String getTypeIcon() {
        return switch (type) {
            case TYPE_NEW_ORDER         -> "🛒";
            case TYPE_LOW_STOCK         -> "⚠️";
            case TYPE_ORDER_COMPLETED   -> "✅";
            case TYPE_RESERVATION_TODAY -> "📅";
            case TYPE_ORDER_STATUS      -> "🔄";
            case TYPE_PAYMENT_VOIDED    -> "❌";
            default -> "🔔";
        };
    }

    public String getRelativeTime() {
        if (createdAt == null) return "";
        long mins = java.time.Duration.between(createdAt, LocalDateTime.now()).toMinutes();
        if (mins < 1)  return "just now";
        if (mins < 60) return mins + "m ago";
        long hrs = mins / 60;
        if (hrs < 24)  return hrs + "h ago";
        return createdAt.format(DateTimeFormatter.ofPattern("MMM d"));
    }
}
