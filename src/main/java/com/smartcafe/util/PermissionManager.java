package com.smartcafe.util;

import com.smartcafe.model.Role;

/**
 * Centralised role-based permission check.
 * All UI components call these methods before showing sensitive actions.
 */
public final class PermissionManager {

    private PermissionManager() {}

    private static Role current() {
        var u = SessionManager.getCurrentUser();
        return u != null ? u.getRole() : null;
    }

    private static boolean is(Role... roles) {
        Role r = current();
        if (r == null) return false;
        for (Role allowed : roles) if (allowed == r) return true;
        return false;
    }

    // ── Module access ─────────────────────────────────────────────────────────

    public static boolean canAccessCategories()  { return is(Role.ADMIN, Role.MANAGER); }
    public static boolean canAccessProducts()    { return is(Role.ADMIN, Role.MANAGER); }
    public static boolean canAccessInventory()   { return is(Role.ADMIN, Role.MANAGER); }
    public static boolean canAccessEmployees()   { return is(Role.ADMIN, Role.MANAGER); }
    public static boolean canAccessReports()     { return is(Role.ADMIN, Role.MANAGER); }
    public static boolean canAccessSettings()    { return is(Role.ADMIN); }
    public static boolean canAccessCustomers()   { return is(Role.ADMIN, Role.MANAGER, Role.CASHIER); }
    public static boolean canAccessPOS()         { return is(Role.ADMIN, Role.MANAGER, Role.CASHIER); }
    public static boolean canAccessOrders()      { return is(Role.ADMIN, Role.MANAGER, Role.CASHIER, Role.KITCHEN_STAFF); }
    public static boolean canAccessBilling()     { return is(Role.ADMIN, Role.MANAGER, Role.CASHIER); }
    public static boolean canAccessReservations(){ return is(Role.ADMIN, Role.MANAGER, Role.CASHIER); }

    // ── Action-level permissions ──────────────────────────────────────────────

    public static boolean canEditProducts()      { return is(Role.ADMIN, Role.MANAGER); }
    public static boolean canDeleteProducts()    { return is(Role.ADMIN); }
    public static boolean canEditInventory()     { return is(Role.ADMIN, Role.MANAGER); }
    public static boolean canRestockInventory()  { return is(Role.ADMIN, Role.MANAGER); }
    public static boolean canVoidBill()          { return is(Role.ADMIN, Role.MANAGER); }
    public static boolean canApplyDiscount()     { return is(Role.ADMIN, Role.MANAGER, Role.CASHIER); }
    public static boolean canManageUsers()       { return is(Role.ADMIN); }

    // ── Order status transitions ──────────────────────────────────────────────

    /** Returns true if the current user can advance order to the given next status. */
    public static boolean canSetOrderStatus(String newStatus) {
        return switch (newStatus) {
            case "CONFIRMED"  -> is(Role.ADMIN, Role.MANAGER, Role.CASHIER);
            case "PREPARING"  -> is(Role.ADMIN, Role.MANAGER, Role.KITCHEN_STAFF);
            case "IN_PROGRESS"-> is(Role.ADMIN, Role.MANAGER, Role.KITCHEN_STAFF);
            case "READY"      -> is(Role.ADMIN, Role.MANAGER, Role.KITCHEN_STAFF);
            case "SERVED"     -> is(Role.ADMIN, Role.MANAGER, Role.KITCHEN_STAFF, Role.CASHIER);
            case "COMPLETED"  -> is(Role.ADMIN, Role.MANAGER, Role.CASHIER);
            case "CANCELLED"  -> is(Role.ADMIN, Role.MANAGER);
            default           -> is(Role.ADMIN);
        };
    }

    // ── Valid transition graph ────────────────────────────────────────────────

    public static String[] nextStatuses(String current) {
        return switch (current) {
            case "NEW"        -> new String[]{"CONFIRMED", "CANCELLED"};
            case "CONFIRMED"  -> new String[]{"PREPARING", "CANCELLED"};
            case "PREPARING"  -> new String[]{"READY"};
            case "IN_PROGRESS"-> new String[]{"READY"};
            case "READY"      -> new String[]{"SERVED"};
            case "SERVED"     -> new String[]{"COMPLETED"};
            default           -> new String[]{};
        };
    }

    public static boolean isValidTransition(String from, String to) {
        for (String valid : nextStatuses(from))
            if (valid.equals(to)) return true;
        return false;
    }
}
