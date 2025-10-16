package ui;

import model.User;

/** Simple in-memory session holder. */
public class Session {
    private static User currentUser;

    public static User getCurrentUser() { return currentUser; }
    public static boolean isLoggedIn()   { return currentUser != null; }
    public static void login(User u)     { currentUser = u; }
    public static void logout()          { currentUser = null; }
}

