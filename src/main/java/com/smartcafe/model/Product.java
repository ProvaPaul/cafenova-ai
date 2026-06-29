package com.smartcafe.model;

import java.time.LocalDateTime;

/**
 * Maps to the {@code menu_items} table.
 * {@code categoryName} is a denormalized JOIN result — not persisted on its own.
 */
public class Product {

    private int           id;
    private int           categoryId;
    private String        categoryName;   // populated via JOIN
    private String        name;
    private String        description;
    private double        price;
    private double        costPrice;
    private String        imagePath;
    private boolean       available;
    private boolean       active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Product() {}

    public int     getId()                              { return id; }
    public void    setId(int id)                        { this.id = id; }
    public int     getCategoryId()                      { return categoryId; }
    public void    setCategoryId(int categoryId)        { this.categoryId = categoryId; }
    public String  getCategoryName()                    { return categoryName; }
    public void    setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String  getName()                            { return name; }
    public void    setName(String name)                 { this.name = name; }
    public String  getDescription()                     { return description; }
    public void    setDescription(String description)   { this.description = description; }
    public double  getPrice()                           { return price; }
    public void    setPrice(double price)               { this.price = price; }
    public double  getCostPrice()                       { return costPrice; }
    public void    setCostPrice(double costPrice)       { this.costPrice = costPrice; }
    public String  getImagePath()                       { return imagePath; }
    public void    setImagePath(String imagePath)       { this.imagePath = imagePath; }
    public boolean isAvailable()                        { return available; }
    public void    setAvailable(boolean available)      { this.available = available; }
    public boolean isActive()                           { return active; }
    public void    setActive(boolean active)            { this.active = active; }
    public LocalDateTime getCreatedAt()                 { return createdAt; }
    public void    setCreatedAt(LocalDateTime c)        { this.createdAt = c; }
    public LocalDateTime getUpdatedAt()                 { return updatedAt; }
    public void    setUpdatedAt(LocalDateTime u)        { this.updatedAt = u; }
}
