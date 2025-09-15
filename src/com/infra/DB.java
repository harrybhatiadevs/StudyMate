package com.infra;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Simple SQLite DB bootstrap.
 * Creates local file 'studymate.db' and ensures required tables exist.
 */
public class DB {
    private static final String URL = "jdbc:sqlite:studymate.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    /** Call this once at app startup (e.g., in StudyMateFrame constructor). */
    public static void ensureInitialized() {
        try (Connection conn = getConnection();
             Statement st = conn.createStatement()) {

            // Users table
            st.execute("""
                CREATE TABLE IF NOT EXISTS users(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  username TEXT NOT NULL UNIQUE,
                  email TEXT NOT NULL UNIQUE,
                  password_hash TEXT NOT NULL,
                  salt TEXT NOT NULL,
                  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                );
            """);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("DB init failed: " + e.getMessage(), e);
        }
    }
}
