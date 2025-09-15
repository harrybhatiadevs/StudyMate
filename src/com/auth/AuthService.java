package com.auth;

import com.infra.DB;
import com.model.Profile;

import java.sql.*;

/**
 * Basic auth service: register and login against SQLite.
 */
public class AuthService {

    public Profile register(String username, String email, String plainPassword) throws SQLException {
        String salt = Passwords.genSalt();
        String hash = Passwords.hash(plainPassword, salt);

        try (Connection conn = DB.getConnection()) {
            // Uniqueness check
            try (PreparedStatement ck = conn.prepareStatement(
                    "SELECT 1 FROM users WHERE username=? OR email=? LIMIT 1")) {
                ck.setString(1, username);
                ck.setString(2, email);
                try (ResultSet rs = ck.executeQuery()) {
                    if (rs.next()) {
                        throw new SQLException("Username or email already exists");
                    }
                }
            }

            // Insert
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users(username,email,password_hash,salt) VALUES(?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username);
                ps.setString(2, email);
                ps.setString(3, hash);
                ps.setString(4, salt);
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        return new Profile(id, username, email);
                    }
                }
            }
        }

        throw new SQLException("Register failed: no generated ID");
    }

    public Profile login(String usernameOrEmail, String plainPassword) throws SQLException {
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, username, email, password_hash, salt FROM users " +
                             "WHERE username=? OR email=? LIMIT 1")) {
            ps.setString(1, usernameOrEmail);
            ps.setString(2, usernameOrEmail);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("User not found");
                }
                String salt = rs.getString("salt");
                String hash = rs.getString("password_hash");
                if (!Passwords.verify(plainPassword, salt, hash)) {
                    throw new SQLException("Invalid password");
                }
                return new Profile(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email")
                );
            }
        }
    }
}
