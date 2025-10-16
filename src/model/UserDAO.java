package model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class UserDAO {

    private final String dbUrl;
    private static final Pattern EMAIL_RE =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.com$"); // xxx@xxx.com

    public UserDAO(String dbUrl) {
        this.dbUrl = dbUrl;
        ensureUsersTable();
    }

    private Connection getConn() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    private void ensureUsersTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS users(
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              username TEXT NOT NULL,          -- not unique
              email TEXT NOT NULL UNIQUE,      -- unique
              password_hash TEXT NOT NULL,
              created_at TEXT NOT NULL
            );
            """;
        try (Connection c = getConn(); Statement st = c.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to ensure users table", e);
        }
    }

    /* ---------- Validation ---------- */

    public static boolean isPasswordValid(String raw) {
        if (raw == null || raw.length() < 6) return false;
        boolean hasLetter = raw.chars().anyMatch(Character::isLetter);
        boolean hasDigit  = raw.chars().anyMatch(Character::isDigit);
        return hasLetter && hasDigit;
    }

    public static boolean isEmailValid(String email) {
        return email != null && EMAIL_RE.matcher(email.trim()).matches();
    }

    public boolean isEmailTaken(String email) {
        String q = "SELECT 1 FROM users WHERE email = ? LIMIT 1";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(q)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* ---------- Create & Auth ---------- */

    public User createUser(String username, String email, String rawPassword) {
        if (!isEmailValid(email)) throw new IllegalArgumentException("Email must be like: name@domain.com");
        if (isEmailTaken(email)) throw new IllegalArgumentException("Email already exists.");
        if (!isPasswordValid(rawPassword))
            throw new IllegalArgumentException("Password must be at least 6 characters and contain letters and digits.");

        String sql = "INSERT INTO users(username, email, password_hash, created_at) VALUES(?,?,?,?)";
        String hash = sha256(rawPassword);
        String createdAt = LocalDateTime.now().toString();

        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username == null ? "" : username.trim());
            ps.setString(2, email.trim());
            ps.setString(3, hash);
            ps.setString(4, createdAt);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    User u = new User(username == null ? "" : username.trim(), email.trim(), hash);
                    u.setId(id);
                    u.setCreatedAt(LocalDateTime.parse(createdAt));
                    return u;
                }
            }
            throw new RuntimeException("Failed to create user: no id returned.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Login by Email + Password */
    public User authenticate(String email, String rawPassword) {
        String q = "SELECT id, username, email, password_hash, created_at FROM users WHERE email = ? LIMIT 1";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(q)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String storedHash = rs.getString("password_hash");
                if (!sha256(rawPassword).equals(storedHash)) return null;

                User u = new User();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setEmail(rs.getString("email"));
                u.setPasswordHash(storedHash);
                u.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
                return u;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* ---------- Profile updates ---------- */

    public void updateUsername(int userId, String newUsername) {
        String sql = "UPDATE users SET username = ? WHERE id = ?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newUsername == null ? "" : newUsername.trim());
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update username", e);
        }
    }

    public void changePassword(int userId, String newRawPassword) {
        if (!isPasswordValid(newRawPassword))
            throw new IllegalArgumentException("Password must be at least 6 characters and contain letters and digits.");
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, sha256(newRawPassword));
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to change password", e);
        }
    }

    /* ---------- Utils ---------- */

    public static String sha256(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
