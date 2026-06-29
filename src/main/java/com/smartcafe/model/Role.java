package com.smartcafe.model;

/**
 * Four fixed staff roles.  Stored as VARCHAR in MySQL so queries read without
 * a join.  The enum gives compile-time safety on the Java side.
 */
public enum Role {

    ADMIN("Admin"),
    MANAGER("Manager"),
    CASHIER("Cashier"),
    KITCHEN_STAFF("Kitchen Staff");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Case-insensitive lookup used when reading the DB column. */
    public static Role fromString(String value) {
        for (Role r : values()) {
            if (r.name().equalsIgnoreCase(value)) return r;
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }
}
