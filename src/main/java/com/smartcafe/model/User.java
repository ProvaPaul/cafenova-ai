package com.smartcafe.model;

import java.time.LocalDateTime;

/** POJO representing a row in the {@code users} table. */
public class User {

    private int           id;
    private String        fullName;
    private String        username;
    private String        email;
    private String        passwordHash;
    private String        phone;
    private Role          role;
    private boolean       active;
    private Integer       createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {}

    /** Convenience constructor used by AuthServiceImpl when creating a new account. */
    public User(String fullName, String username, String email,
                String passwordHash, String phone, Role role) {
        this.fullName     = fullName;
        this.username     = username;
        this.email        = email;
        this.passwordHash = passwordHash;
        this.phone        = phone;
        this.role         = role;
        this.active       = true;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }

    public String getFullName()                     { return fullName; }
    public void setFullName(String fullName)         { this.fullName = fullName; }

    public String getUsername()                     { return username; }
    public void setUsername(String username)         { this.username = username; }

    public String getEmail()                        { return email; }
    public void setEmail(String email)              { this.email = email; }

    public String getPasswordHash()                 { return passwordHash; }
    public void setPasswordHash(String passwordHash){ this.passwordHash = passwordHash; }

    public String getPhone()                        { return phone; }
    public void setPhone(String phone)              { this.phone = phone; }

    public Role getRole()                           { return role; }
    public void setRole(Role role)                  { this.role = role; }

    public boolean isActive()                       { return active; }
    public void setActive(boolean active)           { this.active = active; }

    public Integer getCreatedBy()                   { return createdBy; }
    public void setCreatedBy(Integer createdBy)     { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt()                     { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)       { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt()                     { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)       { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', role=" + role + "}";
    }
}
