package com.auth;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Password hashing with random salt (SHA-256 + Base64).
 */
public class Passwords {

    public static String genSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(String plain, String saltB64) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(saltB64));
            byte[] out = md.digest(plain.getBytes());
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new RuntimeException("Hashing error", e);
        }
    }

    public static boolean verify(String plain, String saltB64, String expectedHashB64) {
        return hash(plain, saltB64).equals(expectedHashB64);
    }
}
