package com.model;

/**
 * Minimal user profile stored in session after login.
 */
public class Profile {
    private final int id;
    private final String username;
    private final String email;

    public Profile(int id, String username, String email){
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public int getId(){ return id; }
    public String getUsername(){ return username; }
    public String getEmail(){ return email; }
}
