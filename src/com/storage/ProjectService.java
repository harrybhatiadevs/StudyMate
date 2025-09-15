package com.storage;

import com.auth.Session;
import com.infra.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ProjectService: basic CRUD using SQLite.
 * Adjust table/columns to your actual schema if different.
 */
public class ProjectService {

    public static void ensureTable() {
        try (Connection c = DB.getConnection();
             Statement st = c.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS projects(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  user_id INTEGER NOT NULL,
                  name TEXT NOT NULL,
                  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                );
            """);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to ensure projects table", e);
        }
    }

    public static int createProject(String name) throws SQLException {
        if (!Session.isLoggedIn()) throw new IllegalStateException("Login required");
        int uid = Session.getCurrentUser().getId();

        ensureTable();
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO projects(user_id, name) VALUES(?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, uid);
            ps.setString(2, name);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Create project failed (no id)");
    }

    public static List<String> listMyProjectNames() throws SQLException {
        if (!Session.isLoggedIn()) throw new IllegalStateException("Login required");
        int uid = Session.getCurrentUser().getId();

        ensureTable();
        List<String> names = new ArrayList<>();
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT name FROM projects WHERE user_id=? ORDER BY id DESC")) {
            ps.setInt(1, uid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) names.add(rs.getString(1));
            }
        }
        return names;
    }
}
