package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FlashcardDAO {
    private static final String DB_URL = "jdbc:sqlite:studymate.db";

    // Constructor: makes sure the table exists
    public FlashcardDAO() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS flashcards (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  question TEXT NOT NULL,
                  answer   TEXT NOT NULL
                )
            """);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // CREATE (insert a new flashcard)
    public void addFlashcard(Flashcard card) {
        String sql = "INSERT INTO flashcards(question, answer) VALUES(?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, card.getQuestion());
            ps.setString(2, card.getAnswer());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // READ (get all flashcards from DB)
    public List<Flashcard> getAllFlashcards() {
        List<Flashcard> list = new ArrayList<>();
        String sql = "SELECT id, question, answer FROM flashcards ORDER BY id";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Flashcard card = new Flashcard(
                        rs.getString("question"),
                        rs.getString("answer")
                );
                card.setId(rs.getInt("id"));
                list.add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // UPDATE (change question/answer by id)
    public void updateFlashcard(Flashcard card) {
        String sql = "UPDATE flashcards SET question = ?, answer = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, card.getQuestion());
            ps.setString(2, card.getAnswer());
            ps.setInt(3, card.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE (remove a flashcard by id)
    public void deleteFlashcard(int id) {
        String sql = "DELETE FROM flashcards WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}