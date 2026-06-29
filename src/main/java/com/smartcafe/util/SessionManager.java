package com.smartcafe.util;

import com.smartcafe.model.User;

/**
 * In-memory session holder for the currently authenticated staff member.
 *
 * This is a simple desktop app with one user per JVM process, so a static
 * field is appropriate.  Do NOT copy this pattern into a web / multi-user
 * server — use an HTTP session or JWT there.
 */
public final class SessionManager {

    private static volatile User currentUser;

    private SessionManager() {}

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void clearCurrentUser() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
