package com.smartcafe.model;

import java.time.LocalDateTime;

/** Maps to the {@code categories} table. */
public class Category {

    private int           id;
    private String        name;
    private String        description;
    private int           sortOrder;
    private boolean       active;
    private LocalDateTime createdAt;

    public Category() {}

    public Category(String name, String description, int sortOrder) {
        this.name        = name;
        this.description = description;
        this.sortOrder   = sortOrder;
        this.active      = true;
    }

    public int    getId()                              { return id; }
    public void   setId(int id)                        { this.id = id; }
    public String getName()                            { return name; }
    public void   setName(String name)                 { this.name = name; }
    public String getDescription()                     { return description; }
    public void   setDescription(String d)             { this.description = d; }
    public int    getSortOrder()                       { return sortOrder; }
    public void   setSortOrder(int sortOrder)          { this.sortOrder = sortOrder; }
    public boolean isActive()                          { return active; }
    public void   setActive(boolean active)            { this.active = active; }
    public LocalDateTime getCreatedAt()                { return createdAt; }
    public void   setCreatedAt(LocalDateTime c)        { this.createdAt = c; }

    /** Used by JComboBox to render the display name. */
    @Override
    public String toString() { return name; }
}
