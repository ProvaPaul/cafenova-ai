package com.smartcafe.util;

import java.security.SecureRandom;
import java.util.Base64;

/** Generates cryptographically secure tokens for password resets. */
public final class TokenUtil {

    private static final SecureRandom RNG = new SecureRandom();

    private TokenUtil() {}

    /**
     * Returns a 43-character URL-safe Base64 token backed by 32 random bytes.
     * Used as the password-reset token stored in the DB and (in production) emailed.
     */
    public static String generateToken() {
        byte[] bytes = new byte[32];
        RNG.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Returns a 6-digit numeric code — an easier-to-type alternative for
     * in-person verification scenarios (displayed on screen, entered manually).
     */
    public static String generateOtp() {
        return String.format("%06d", RNG.nextInt(1_000_000));
    }
}
