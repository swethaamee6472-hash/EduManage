package com.sset.erp.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for cryptographic password hashing and validation.
 * Utilizes SHA-256 with unique per-user cryptographic salts and constant-time comparison.
 */
public final class PasswordUtil {
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
        // Private constructor for static utility class
    }

    /**
     * Generates a cryptographically strong random salt (16 bytes, Base64).
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes a raw password combined with a salt using SHA-256.
     */
    public static String hashPassword(String password, String salt) {
        if (password == null || salt == null) {
            throw new IllegalArgumentException("Password and salt must not be null");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashedBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Fatal error: SHA-256 algorithm not available", e);
        }
    }

    /**
     * Verifies a raw password against an expected hash and salt using constant-time comparison.
     */
    public static boolean verifyPassword(String rawPassword, String expectedHash, String salt) {
        if (rawPassword == null || expectedHash == null || salt == null) {
            return false;
        }
        String computedHash = hashPassword(rawPassword, salt);
        byte[] expected = expectedHash.getBytes(StandardCharsets.UTF_8);
        byte[] actual = computedHash.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }
}
