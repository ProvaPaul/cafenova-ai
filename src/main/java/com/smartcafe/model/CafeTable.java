package com.smartcafe.model;

import java.time.LocalDateTime;

public class CafeTable {

    public static final String STATUS_AVAILABLE   = "AVAILABLE";
    public static final String STATUS_OCCUPIED    = "OCCUPIED";
    public static final String STATUS_RESERVED    = "RESERVED";
    public static final String STATUS_MAINTENANCE = "MAINTENANCE";

    private int           id;
    private String        tableNumber;
    private int           capacity;
    private String        location;
    private String        status;
    private LocalDateTime createdAt;

    public CafeTable() { this.status = STATUS_AVAILABLE; }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public int     getId()        { return id; }
    public void    setId(int v)   { id = v; }

    public String  getTableNumber()          { return tableNumber; }
    public void    setTableNumber(String v)  { tableNumber = v; }

    public int     getCapacity()       { return capacity; }
    public void    setCapacity(int v)  { capacity = v; }

    public String  getLocation()          { return location; }
    public void    setLocation(String v)  { location = v; }

    public String  getStatus()           { return status; }
    public void    setStatus(String v)   { status = v; }

    public LocalDateTime getCreatedAt()               { return createdAt; }
    public void          setCreatedAt(LocalDateTime v) { createdAt = v; }

    /** Display label shown in POS combo: "T01 (4 pax) – Indoor" */
    @Override public String toString() {
        String loc = location != null ? " – " + location : "";
        return tableNumber + " (" + capacity + " pax)" + loc;
    }
}
