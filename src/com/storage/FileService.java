package com.storage;

import com.auth.Session;
import com.infra.DB;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * FileService
 * - Copies uploaded files into a per-user/per-project folder tree.
 * - Persists file metadata into SQLite (files table).
 */
public class FileService {

    /** Root folder for stored files (relative to working directory). */
    private static final Path ROOT = Path.of("data");

    /**
     * Ensure storage directory and DB table exist.
     * Safe to call many times.
     */
    public static void init() {
        try {
            Files.createDirectories(ROOT);
            ensureFilesTable();
        } catch (Exception ignored) {
        }
    }

    /** Create the 'files' table if it does not exist yet. */
    private static void ensureFilesTable() {
        try (Connection c = DB.getConnection();
             Statement st = c.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS files(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  user_id INTEGER NOT NULL,
                  project_id INTEGER NOT NULL,
                  original_name TEXT NOT NULL,
                  stored_path  TEXT NOT NULL,
                  size_bytes   INTEGER NOT NULL,
                  created_at   DATETIME DEFAULT CURRENT_TIMESTAMP
                );
            """);
        } catch (Exception e) {
            throw new RuntimeException("Failed to ensure files table", e);
        }
    }

    /**
     * Copy the file to data/{userId}/{projectId}/ and record a DB row.
     *
     * @param source    local file to store
     * @param projectId project id to associate with
     */
    public static void uploadToProject(File source, int projectId) throws Exception {
        if (!Session.isLoggedIn()) {
            throw new IllegalStateException("Login required");
        }

        int uid = Session.getCurrentUser().getId();
        init();

        // Folder: data/{userId}/{projectId}/
        Path dir = ROOT.resolve(String.valueOf(uid))
                .resolve(String.valueOf(projectId));
        Files.createDirectories(dir);

        Path dest = dir.resolve(source.getName());
        Files.copy(source.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

        // Insert metadata
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO files(user_id, project_id, original_name, stored_path, size_bytes) " +
                             "VALUES(?,?,?,?,?)")) {
            ps.setInt(1, uid);
            ps.setInt(2, projectId);
            ps.setString(3, source.getName());
            ps.setString(4, dest.toString());
            ps.setLong(5, source.length());
            ps.executeUpdate();
        }
    }
}
