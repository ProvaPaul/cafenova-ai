package com.smartcafe.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Reservation {
    public static final String STATUS_PENDING   = "PENDING";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_NO_SHOW   = "NO_SHOW";

    private int id;
    private Integer tableId, customerId;
    private String customerName;
    private int partySize;
    private LocalDate reservationDate;
    private LocalTime reservationTime;
    private String status, notes;
    private LocalDateTime createdAt;
    // transient — joined fields
    private String tableNumber, customerFullName;

    public Reservation() {
        this.status    = STATUS_PENDING;
        this.partySize = 1;
    }

    public int getId()                           { return id; }
    public void setId(int id)                    { this.id = id; }
    public Integer getTableId()                  { return tableId; }
    public void setTableId(Integer v)            { this.tableId = v; }
    public Integer getCustomerId()               { return customerId; }
    public void setCustomerId(Integer v)         { this.customerId = v; }
    public String getCustomerName()              { return customerName; }
    public void setCustomerName(String v)        { this.customerName = v; }
    public int getPartySize()                    { return partySize; }
    public void setPartySize(int v)              { this.partySize = v; }
    public LocalDate getReservationDate()        { return reservationDate; }
    public void setReservationDate(LocalDate v)  { this.reservationDate = v; }
    public LocalTime getReservationTime()        { return reservationTime; }
    public void setReservationTime(LocalTime v)  { this.reservationTime = v; }
    public String getStatus()                    { return status; }
    public void setStatus(String v)              { this.status = v; }
    public String getNotes()                     { return notes; }
    public void setNotes(String v)               { this.notes = v; }
    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void setCreatedAt(LocalDateTime v)    { this.createdAt = v; }
    public String getTableNumber()               { return tableNumber; }
    public void setTableNumber(String v)         { this.tableNumber = v; }
    public String getCustomerFullName()          { return customerFullName; }
    public void setCustomerFullName(String v)    { this.customerFullName = v; }
}
