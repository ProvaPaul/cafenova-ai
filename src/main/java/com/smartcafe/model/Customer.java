package com.smartcafe.model;

import java.time.LocalDateTime;

public class Customer {
    private int id;
    private String fullName, phone, email, address;
    private int loyaltyPoints;
    private double totalSpent;
    private int visitCount;
    private boolean active;
    private LocalDateTime createdAt;

    public Customer() { this.active = true; }

    public Customer(String fullName, String phone, String email, String address) {
        this.fullName = fullName; this.phone = phone;
        this.email = email; this.address = address;
        this.active = true;
    }

    public int getId()                    { return id; }
    public void setId(int id)             { this.id = id; }
    public String getFullName()           { return fullName; }
    public void setFullName(String v)     { this.fullName = v; }
    public String getPhone()              { return phone; }
    public void setPhone(String v)        { this.phone = v; }
    public String getEmail()              { return email; }
    public void setEmail(String v)        { this.email = v; }
    public String getAddress()            { return address; }
    public void setAddress(String v)      { this.address = v; }
    public int getLoyaltyPoints()         { return loyaltyPoints; }
    public void setLoyaltyPoints(int v)   { this.loyaltyPoints = v; }
    public double getTotalSpent()         { return totalSpent; }
    public void setTotalSpent(double v)   { this.totalSpent = v; }
    public int getVisitCount()            { return visitCount; }
    public void setVisitCount(int v)      { this.visitCount = v; }
    public boolean isActive()             { return active; }
    public void setActive(boolean v)      { this.active = v; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }

    @Override public String toString() { return fullName; }
}
