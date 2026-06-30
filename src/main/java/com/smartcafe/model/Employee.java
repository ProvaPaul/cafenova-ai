package com.smartcafe.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Employee {
    private int id;
    private String fullName, phone, email, address, position, department;
    private double baseSalary;
    private LocalDate hireDate;
    private boolean active;
    private Integer userId;
    private LocalDateTime createdAt;

    public Employee() { this.active = true; }

    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }
    public String getFullName()             { return fullName; }
    public void setFullName(String v)       { this.fullName = v; }
    public String getPhone()                { return phone; }
    public void setPhone(String v)          { this.phone = v; }
    public String getEmail()                { return email; }
    public void setEmail(String v)          { this.email = v; }
    public String getAddress()              { return address; }
    public void setAddress(String v)        { this.address = v; }
    public String getPosition()             { return position; }
    public void setPosition(String v)       { this.position = v; }
    public String getDepartment()           { return department; }
    public void setDepartment(String v)     { this.department = v; }
    public double getBaseSalary()           { return baseSalary; }
    public void setBaseSalary(double v)     { this.baseSalary = v; }
    public LocalDate getHireDate()          { return hireDate; }
    public void setHireDate(LocalDate v)    { this.hireDate = v; }
    public boolean isActive()               { return active; }
    public void setActive(boolean v)        { this.active = v; }
    public Integer getUserId()              { return userId; }
    public void setUserId(Integer v)        { this.userId = v; }
    public LocalDateTime getCreatedAt()     { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }

    @Override public String toString() { return fullName; }
}
