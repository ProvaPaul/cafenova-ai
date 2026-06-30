package com.smartcafe.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Attendance {
    public static final String STATUS_PRESENT  = "PRESENT";
    public static final String STATUS_ABSENT   = "ABSENT";
    public static final String STATUS_LATE     = "LATE";
    public static final String STATUS_HALF_DAY = "HALF_DAY";
    public static final String STATUS_LEAVE    = "LEAVE";

    private int id, employeeId;
    private LocalDate date;
    private LocalTime timeIn, timeOut;
    private String status, notes;
    private LocalDateTime createdAt;
    // transient — joined from employees
    private String employeeName;

    public Attendance() { this.status = STATUS_PRESENT; }

    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }
    public int getEmployeeId()              { return employeeId; }
    public void setEmployeeId(int v)        { this.employeeId = v; }
    public LocalDate getDate()              { return date; }
    public void setDate(LocalDate v)        { this.date = v; }
    public LocalTime getTimeIn()            { return timeIn; }
    public void setTimeIn(LocalTime v)      { this.timeIn = v; }
    public LocalTime getTimeOut()           { return timeOut; }
    public void setTimeOut(LocalTime v)     { this.timeOut = v; }
    public String getStatus()               { return status; }
    public void setStatus(String v)         { this.status = v; }
    public String getNotes()                { return notes; }
    public void setNotes(String v)          { this.notes = v; }
    public LocalDateTime getCreatedAt()     { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
    public String getEmployeeName()         { return employeeName; }
    public void setEmployeeName(String v)   { this.employeeName = v; }
}
