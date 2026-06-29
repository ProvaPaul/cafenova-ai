package com.smartcafe.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Thin wrapper around jBCrypt.
 *
 * Work factor 12 means ~250ms on a modern CPU — painful for an attacker
 * iterating millions of candidates, imperceptible for a user logging in once.
 */
public final class PasswordUtil {

    private static final int WORK_FACTOR = 12;

    private PasswordUtil() {}

    /** Returns a BCrypt hash that embeds its own salt. Store this in the DB. */
    public static String hash(String plainText) {
        return BCrypt.hashpw(plainText, BCrypt.gensalt(WORK_FACTOR));
    }

    /** Constant-time comparison — safe against timing attacks. */
    public static boolean verify(String plainText, String hash) {
        try {
            return BCrypt.checkpw(plainText, hash);
        } catch (Exception e) {
            // Malformed hash or null — treat as mismatch, not an error
            return false;
        }
    }
}
