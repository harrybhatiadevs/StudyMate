package com.auth;

import com.model.Profile;

/**
 * In-memory session holder for the current user.
 */
public class Session {
    private static Profile currentUser;

    public static Profile getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void setCurrentUser(Profile p) {
        currentUser = p;
    }

    public static void logout() {
        currentUser = null;
    }
}
