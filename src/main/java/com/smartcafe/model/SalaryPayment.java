package com.smartcafe.model;

import java.time.LocalDateTime;

public class SalaryPayment {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PAID    = "PAID";

    private int id, employeeId;
    private int periodMonth, periodYear;
    private double baseSalary, bonus, deductions, netSalary;
    private String status, notes;
    private LocalDateTime paidAt, createdAt;
    // transient — joined from employees
    private String employeeName;

    public SalaryPayment() { this.status = STATUS_PENDING; }

    public int getId()                       { return id; }
    public void setId(int id)                { this.id = id; }
    public int getEmployeeId()               { return employeeId; }
    public void setEmployeeId(int v)         { this.employeeId = v; }
    public int getPeriodMonth()              { return periodMonth; }
    public void setPeriodMonth(int v)        { this.periodMonth = v; }
    public int getPeriodYear()               { return periodYear; }
    public void setPeriodYear(int v)         { this.periodYear = v; }
    public double getBaseSalary()            { return baseSalary; }
    public void setBaseSalary(double v)      { this.baseSalary = v; }
    public double getBonus()                 { return bonus; }
    public void setBonus(double v)           { this.bonus = v; }
    public double getDeductions()            { return deductions; }
    public void setDeductions(double v)      { this.deductions = v; }
    public double getNetSalary()             { return netSalary; }
    public void setNetSalary(double v)       { this.netSalary = v; }
    public String getStatus()                { return status; }
    public void setStatus(String v)          { this.status = v; }
    public String getNotes()                 { return notes; }
    public void setNotes(String v)           { this.notes = v; }
    public LocalDateTime getPaidAt()         { return paidAt; }
    public void setPaidAt(LocalDateTime v)   { this.paidAt = v; }
    public LocalDateTime getCreatedAt()      { return createdAt; }
    public void setCreatedAt(LocalDateTime v){ this.createdAt = v; }
    public String getEmployeeName()          { return employeeName; }
    public void setEmployeeName(String v)    { this.employeeName = v; }
}
