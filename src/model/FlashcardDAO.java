package model;

import java.sql.*;
import java.util.*;

public class FlashcardDAO {
    private final String jdbcUrl;

    public FlashcardDAO(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl; // Example: "jdbc:sqlite:studymate.db"
    }

    /** Initialise the database (create table if not exists). */
    public void init() throws SQLException {
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             Statement st = conn.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS flashcards (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    topic TEXT,
                    question TEXT NOT NULL,
                    answer  TEXT NOT NULL
                )
            """);
        }
    }

    /** Get the total number of flashcards. */
    public int count() throws SQLException {
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM flashcards")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Insert a new flashcard into the table. */
    public void insert(Flashcard card) throws SQLException {
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO flashcards(topic, question, answer) VALUES (?,?,?)")) {
            ps.setString(1, card.getTopic());
            ps.setString(2, card.getQuestion());
            ps.setString(3, card.getAnswer());
            ps.executeUpdate();
        }
    }

    /**
     * Return a random flashcard.
     * If topic is provided, select randomly from that topic first.
     */
    public Optional<Flashcard> random(String topic) throws SQLException {
        String sql = (topic != null && !topic.isBlank())
                ? "SELECT topic, question, answer FROM flashcards WHERE topic = ? ORDER BY RANDOM() LIMIT 1"
                : "SELECT topic, question, answer FROM flashcards ORDER BY RANDOM() LIMIT 1";
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (topic != null && !topic.isBlank()) ps.setString(1, topic);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Flashcard(
                            rs.getString("topic"),
                            rs.getString("question"),
                            rs.getString("answer")));
                }
                return Optional.empty();
            }
        }
    }
}
