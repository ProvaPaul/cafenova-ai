package com.smartcafe.util;

import com.smartcafe.exception.ValidationException;

import java.util.regex.Pattern;

/** Stateless field-validation helpers shared across Service classes. */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$");

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9_]{3,50}$");

    private ValidationUtil() {}

    /** Throws {@link ValidationException} if value is null or blank. */
    public static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " is required");
        }
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /** Username: 3-50 characters, letters/digits/underscores only. */
    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    /** Returns true when the two values are equal and non-null. */
    public static boolean matches(String a, String b) {
        return a != null && a.equals(b);
    }
}
