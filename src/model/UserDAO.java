package model;

import java.sql.*;
import java.security.MessageDigest;
import java.util.HexFormat;

public class UserDAO {
    private final String jdbcUrl;

    public UserDAO(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl; // e.g. "jdbc:sqlite:studymate.db"
    }

    /** Create table if not exists (username/email are UNIQUE). */
    public void init() throws SQLException {
        try (Connection c = DriverManager.getConnection(jdbcUrl);
             Statement st = c.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  username TEXT UNIQUE NOT NULL,
                  email    TEXT UNIQUE NOT NULL,
                  password TEXT NOT NULL
                )
            """);
        }
    }

    /** Check if a username already exists (case-sensitive). */
    public boolean usernameExists(String username) throws SQLException {
        String u = username == null ? "" : username.trim();
        String sql = "SELECT 1 FROM users WHERE username=? LIMIT 1";
        try (Connection c = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Check if an email already exists (case-insensitive). */
    public boolean emailExists(String email) throws SQLException {
        String em = (email == null ? "" : email.trim().toLowerCase());
        String sql = "SELECT 1 FROM users WHERE LOWER(email)=? LIMIT 1";
        try (Connection c = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, em);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Register a new user (validates fields, email format, password length, uniqueness). */
    public User register(String username, String email, String passwordPlain) throws Exception {
        String u = username == null ? "" : username.trim();
        String em = email == null ? "" : email.trim().toLowerCase();

        // Required fields
        if (u.isEmpty() || em.isEmpty() || passwordPlain == null || passwordPlain.isEmpty()) {
            throw new IllegalArgumentException("All fields are required.");
        }
        // Email format (simple validation)
        if (!em.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        // Password length
        if (passwordPlain.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        // Uniqueness pre-checks for friendly messages
        if (usernameExists(u)) {
            throw new IllegalArgumentException("Username already taken.");
        }
        if (emailExists(em)) {
            throw new IllegalArgumentException("Email already registered.");
        }

        String pwdHash = sha256(passwordPlain);
        try (Connection c = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO users(username,email,password) VALUES(?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u);
            ps.setString(2, em);
            ps.setString(3, pwdHash);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return new User(rs.getInt(1), u, em);
                }
            }
        } catch (SQLException e) {
            // In case UNIQUE constraint triggers due to a race
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique")) {
                throw new IllegalArgumentException("Username or email already exists.");
            }
            throw e;
        }
        throw new SQLException("Failed to create user.");
    }

    /** Login with username OR email + password. Email is matched case-insensitively. */
    public User login(String usernameOrEmail, String passwordPlain) throws Exception {
        String id = usernameOrEmail == null ? "" : usernameOrEmail.trim();
        String idEmailLower = id.toLowerCase();
        String pwdHash = sha256(passwordPlain);

        String sql = """
            SELECT id, username, email
            FROM users
            WHERE (username = ? OR LOWER(email) = ?) AND password = ?
            """;
        try (Connection c = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, idEmailLower);
            ps.setString(3, pwdHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("id"), rs.getString("username"), rs.getString("email"));
                }
            }
        }
        return null;
    }

    /** Update username (still unique). */
    public boolean updateUsername(int userId, String newUsername) throws SQLException {
        String u = newUsername == null ? "" : newUsername.trim();
        if (u.isEmpty()) return false;

        // Optional pre-check for a friendlier message
        if (usernameExists(u)) {
            throw new SQLException("Username already taken.");
        }

        String sql = "UPDATE users SET username=? WHERE id=?";
        try (Connection c = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        }
    }

    /** Change password after verifying the old password. */
    public boolean changePassword(int userId, String oldPassword, String newPassword) throws Exception {
        // Verify old password
        String check = "SELECT password FROM users WHERE id=?";
        try (Connection c = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = c.prepareStatement(check)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                String oldHash = rs.getString(1);
                if (!oldHash.equals(sha256(oldPassword))) return false;
            }
        }
        // Enforce new password length
        if (newPassword == null || newPassword.length() < 6) {
            return false;
        }
        // Update password
        String upd = "UPDATE users SET password=? WHERE id=?";
        try (Connection c = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = c.prepareStatement(upd)) {
            ps.setString(1, sha256(newPassword));
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        }
    }

    // --- simple SHA-256 helper (demo purpose only) ---
    private static String sha256(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] dig = md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(dig);
    }
}
