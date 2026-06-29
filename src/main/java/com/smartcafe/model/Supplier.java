package com.smartcafe.model;

import java.time.LocalDateTime;

public class Supplier {

    private int           id;
    private String        name;
    private String        contact;
    private String        phone;
    private String        email;
    private String        address;
    private boolean       active;
    private LocalDateTime createdAt;

    public Supplier() {}

    public Supplier(String name) { this.name = name; this.active = true; }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public int           getId()        { return id; }
    public void          setId(int v)   { id = v; }

    public String        getName()           { return name; }
    public void          setName(String v)   { name = v; }

    public String        getContact()           { return contact; }
    public void          setContact(String v)   { contact = v; }

    public String        getPhone()           { return phone; }
    public void          setPhone(String v)   { phone = v; }

    public String        getEmail()           { return email; }
    public void          setEmail(String v)   { email = v; }

    public String        getAddress()           { return address; }
    public void          setAddress(String v)   { address = v; }

    public boolean       isActive()          { return active; }
    public void          setActive(boolean v) { active = v; }

    public LocalDateTime getCreatedAt()           { return createdAt; }
    public void          setCreatedAt(LocalDateTime v) { createdAt = v; }

    @Override public String toString() { return name != null ? name : "—"; }
}
